package com.borjaglez.dddlab.ordering.infrastructure.out.persistence;

import com.borjaglez.dddlab.ordering.application.port.in.OrderView;
import com.borjaglez.dddlab.ordering.domain.CustomerId;
import com.borjaglez.dddlab.ordering.domain.Order;
import com.borjaglez.dddlab.ordering.domain.OrderId;
import com.borjaglez.dddlab.ordering.domain.OrderLine;
import com.borjaglez.dddlab.ordering.domain.OrderStatus;
import com.borjaglez.dddlab.ordering.domain.PaymentId;
import com.borjaglez.dddlab.ordering.domain.ProductId;
import com.borjaglez.dddlab.ordering.domain.Quantity;
import com.borjaglez.dddlab.sharedkernel.Money;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;

/**
 * Traduce entre el modelo de dominio y el de persistencia. Es el coste de no tener anotaciones JPA
 * en el dominio: unas decenas de líneas a cambio de un {@code Order} que solo habla de negocio.
 */
final class OrderMapper {

  private OrderMapper() {}

  /** Base de datos → dominio. Usa {@code reconstitute}: cargar un pedido no es un hecho nuevo. */
  static Order toDomain(OrderJpaEntity entity) {
    List<OrderLine> lines =
        entity.getLines().stream()
            .map(
                line ->
                    new OrderLine(
                        new ProductId(line.getProductId()),
                        line.getProductName(),
                        new Money(line.getUnitPrice(), Currency.getInstance(line.getCurrency())),
                        new Quantity(line.getQuantity())))
            .toList();
    return Order.reconstitute(
        new OrderId(entity.getId()),
        new CustomerId(entity.getCustomerId()),
        lines,
        entity.getPlacedAt(),
        OrderStatus.valueOf(entity.getStatus()),
        entity.getPaymentId() == null ? null : new PaymentId(entity.getPaymentId()));
  }

  /** Dominio → base de datos, sobre la entidad gestionada para que @Version detecte conflictos. */
  static void copyToEntity(Order order, OrderJpaEntity entity) {
    entity.setCustomerId(order.customerId().value());
    entity.setStatus(order.status().name());
    entity.setPaymentId(order.paymentId().map(PaymentId::value).orElse(null));
    entity.setPlacedAt(order.placedAt());
    entity.getLines().clear();
    order
        .lines()
        .forEach(
            line ->
                entity
                    .getLines()
                    .add(
                        new OrderLineEmbeddable(
                            line.productId().value(),
                            line.productName(),
                            line.unitPrice().amount(),
                            line.unitPrice().currency().getCurrencyCode(),
                            line.quantity().value())));
  }

  /** Base de datos → vista de lectura, sin pasar por el agregado (lado de lectura de CQRS). */
  static OrderView toView(OrderJpaEntity entity) {
    List<OrderView.Line> lines =
        entity.getLines().stream()
            .map(
                line ->
                    new OrderView.Line(
                        line.getProductId(),
                        line.getProductName(),
                        line.getUnitPrice(),
                        line.getQuantity(),
                        line.getUnitPrice().multiply(BigDecimal.valueOf(line.getQuantity()))))
            .toList();
    BigDecimal total =
        lines.stream().map(OrderView.Line::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    String currency =
        entity.getLines().isEmpty() ? null : entity.getLines().getFirst().getCurrency();
    return new OrderView(
        entity.getId().toString(),
        entity.getCustomerId(),
        entity.getStatus(),
        lines,
        total,
        currency,
        entity.getPaymentId(),
        entity.getPlacedAt());
  }
}
