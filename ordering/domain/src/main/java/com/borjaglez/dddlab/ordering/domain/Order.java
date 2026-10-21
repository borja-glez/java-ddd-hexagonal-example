package com.borjaglez.dddlab.ordering.domain;

import static java.util.Objects.requireNonNull;

import com.borjaglez.dddlab.ordering.domain.event.OrderCancelled;
import com.borjaglez.dddlab.ordering.domain.event.OrderPaid;
import com.borjaglez.dddlab.ordering.domain.event.OrderPlaced;
import com.borjaglez.dddlab.ordering.domain.event.OrderShipped;
import com.borjaglez.dddlab.ordering.domain.exception.EmptyOrder;
import com.borjaglez.dddlab.ordering.domain.exception.IllegalOrderStateTransition;
import com.borjaglez.dddlab.ordering.domain.exception.OrderAlreadyShipped;
import com.borjaglez.dddlab.ordering.domain.exception.OrderCannotBeModified;
import com.borjaglez.dddlab.ordering.domain.exception.TooManyLines;
import com.borjaglez.dddlab.sharedkernel.Money;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Optional;

/**
 * [DDD · Aggregate Root + Entity] El pedido.
 *
 * <p>Es la única puerta de entrada al agregado (el pedido y sus {@link OrderLine}) y protege sus
 * invariantes, las reglas que se cumplen siempre:
 *
 * <ol>
 *   <li>Un pedido tiene entre 1 y {@link #MAX_LINES} líneas, todas en la misma divisa.
 *   <li>Solo se añaden productos mientras el pedido está {@code PLACED}.
 *   <li>El estado cambia siguiendo {@link OrderStatus}; un pedido enviado no se puede cancelar.
 *   <li>El total no se guarda: se calcula de las líneas, así nunca se desincroniza.
 * </ol>
 *
 * <p>Modelo rico ("tell, don't ask"): no hay setters. El estado cambia con verbos del negocio
 * ({@code place}, {@code addLine}, {@code markAsPaid}, {@code ship}, {@code cancel}) y cada cambio
 * relevante anota un evento de dominio. No conoce Spring, JPA ni HTTP.
 */
public final class Order extends AggregateRoot {

  public static final int MAX_LINES = 10;

  /** Un pedido sin pagar caduca a los 30 minutos (lo aplica el caso de uso CancelExpiredOrders). */
  public static final Duration PAYMENT_DEADLINE = Duration.ofMinutes(30);

  private final OrderId id;
  // [Vernon] Otro agregado (el cliente, de otro contexto) se referencia solo por su id.
  private final CustomerId customerId;
  private final List<OrderLine> lines;
  private final Instant placedAt;
  private OrderStatus status;
  private PaymentId paymentId;

  private Order(
      OrderId id,
      CustomerId customerId,
      List<OrderLine> lines,
      Instant placedAt,
      OrderStatus status,
      PaymentId paymentId) {
    this.id = requireNonNull(id, "id");
    this.customerId = requireNonNull(customerId, "customerId");
    this.lines = new ArrayList<>(lines);
    this.placedAt = requireNonNull(placedAt, "placedAt");
    this.status = requireNonNull(status, "status");
    this.paymentId = paymentId;
  }

  /**
   * [DDD · Factory] Realiza un pedido nuevo. Es la única forma de crear un pedido desde cero: valida
   * todas las invariantes y anota {@link OrderPlaced}. No hay forma de tener un Order a medio hacer.
   */
  public static Order place(OrderId id, CustomerId customerId, List<OrderLine> lines, Instant now) {
    if (lines.isEmpty()) {
      throw new EmptyOrder();
    }
    Order order = new Order(id, customerId, List.of(), now, OrderStatus.PLACED, null);
    lines.forEach(order::addLine);
    order.recordEvent(new OrderPlaced(id, customerId, order.total(), now));
    return order;
  }

  /**
   * Reconstruye un pedido que ya existía (lo usa el adaptador de persistencia al cargarlo). No es
   * una creación: no emite eventos, porque no ha pasado nada nuevo en el negocio.
   */
  public static Order reconstitute(
      OrderId id,
      CustomerId customerId,
      List<OrderLine> lines,
      Instant placedAt,
      OrderStatus status,
      PaymentId paymentId) {
    return new Order(id, customerId, lines, placedAt, status, paymentId);
  }

  /**
   * Añade un producto. Siempre a través de la raíz, nunca con {@code order.lines().add(...)}: la
   * raíz comprueba que el pedido aún se puede modificar y que no supera el máximo de líneas.
   *
   * <p>Si el producto ya está en el pedido, la línea existente suma la cantidad y conserva su precio
   * congelado: la línea es una entidad identificada por el producto.
   */
  public void addLine(OrderLine line) {
    if (status != OrderStatus.PLACED) {
      throw new OrderCannotBeModified(id, status);
    }
    if (!lines.isEmpty() && !line.unitPrice().currency().equals(currency())) {
      throw new IllegalArgumentException("Todas las líneas de un pedido van en la misma divisa");
    }
    Optional<OrderLine> existing = lineFor(line.productId());
    if (existing.isPresent()) {
      existing.get().increaseBy(line.quantity());
      return;
    }
    if (lines.size() == MAX_LINES) {
      throw new TooManyLines(MAX_LINES);
    }
    lines.add(line);
  }

  /** Comprueba, antes de cobrar, que el pedido admite el pago: no se cobra un pedido cancelado. */
  public void ensureCanBePaid() {
    requireTransitionTo(OrderStatus.PAID, "pagar");
  }

  public void markAsPaid(PaymentId paymentId, Instant now) {
    requireTransitionTo(OrderStatus.PAID, "pagar");
    this.paymentId = requireNonNull(paymentId, "paymentId");
    this.status = OrderStatus.PAID;
    recordEvent(new OrderPaid(id, customerId, paymentId, total(), now));
  }

  public void ship(Instant now) {
    requireTransitionTo(OrderStatus.SHIPPED, "enviar");
    this.status = OrderStatus.SHIPPED;
    recordEvent(new OrderShipped(id, customerId, now));
  }

  /**
   * Cancela el pedido. Regla de negocio: un pedido enviado no se puede cancelar. Si estaba pagado
   * hay que reembolsarlo, pero el pedido no llama a la pasarela: anota {@link OrderCancelled} y la
   * política de reembolso reacciona al evento. Así ningún camino (la API, el job de caducados) puede
   * cancelar sin pasar por esta regla.
   */
  public void cancel(CancellationReason reason, Instant now) {
    if (status == OrderStatus.SHIPPED) {
      throw new OrderAlreadyShipped(id);
    }
    requireTransitionTo(OrderStatus.CANCELLED, "cancelar");
    this.status = OrderStatus.CANCELLED;
    recordEvent(new OrderCancelled(id, customerId, reason, paymentId, total(), now));
  }

  public Money total() {
    return lines.stream().map(OrderLine::subtotal).reduce(Money::add).orElseThrow();
  }

  public OrderId id() {
    return id;
  }

  public CustomerId customerId() {
    return customerId;
  }

  /** Copia de solo lectura: desde fuera se puede mirar, pero no tocar. */
  public List<OrderLine> lines() {
    return List.copyOf(lines);
  }

  public Instant placedAt() {
    return placedAt;
  }

  public OrderStatus status() {
    return status;
  }

  public Optional<PaymentId> paymentId() {
    return Optional.ofNullable(paymentId);
  }

  private Optional<OrderLine> lineFor(ProductId productId) {
    return lines.stream().filter(line -> line.productId().equals(productId)).findFirst();
  }

  private Currency currency() {
    return lines.getFirst().unitPrice().currency();
  }

  private void requireTransitionTo(OrderStatus target, String action) {
    if (!status.canTransitionTo(target)) {
      throw new IllegalOrderStateTransition(id, status, action);
    }
  }

  /** [DDD · Entity] Igualdad por identidad: dos pedidos con los mismos datos siguen siendo dos. */
  @Override
  public boolean equals(Object other) {
    return other instanceof Order order && id.equals(order.id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}
