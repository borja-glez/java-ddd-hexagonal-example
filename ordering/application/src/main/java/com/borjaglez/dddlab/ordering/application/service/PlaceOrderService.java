package com.borjaglez.dddlab.ordering.application.service;

import static java.util.Objects.requireNonNull;

import com.borjaglez.dddlab.ordering.application.exception.ProductNotFound;
import com.borjaglez.dddlab.ordering.application.port.in.PlaceOrderCommand;
import com.borjaglez.dddlab.ordering.application.port.in.PlaceOrderUseCase;
import com.borjaglez.dddlab.ordering.application.port.out.CatalogProduct;
import com.borjaglez.dddlab.ordering.application.port.out.DomainEventPublisher;
import com.borjaglez.dddlab.ordering.application.port.out.ProductCatalog;
import com.borjaglez.dddlab.ordering.domain.Order;
import com.borjaglez.dddlab.ordering.domain.OrderId;
import com.borjaglez.dddlab.ordering.domain.OrderLine;
import com.borjaglez.dddlab.ordering.domain.OrderRepository;
import com.borjaglez.dddlab.ordering.domain.UnpaidOrdersLimit;
import java.time.Clock;
import java.util.List;

/**
 * [DDD · Application Service] Caso de uso "realizar un pedido".
 *
 * <p>Orquesta y no decide: cada regla la aplica el dominio. Fíjate en que no hay ningún {@code if}
 * de negocio aquí. Los pasos son siempre los mismos:
 *
 * <ol>
 *   <li>Pide al dominio que compruebe las reglas que miran varios agregados (servicio de dominio).
 *   <li>Trae del exterior lo que haga falta, a través de puertos de salida (precios del catálogo).
 *   <li>Crea o carga el agregado y le pide que haga el cambio (factory {@code Order.place}).
 *   <li>Lo guarda y publica los eventos que el agregado ha anotado.
 * </ol>
 *
 * <p>Es Java puro: no lleva {@code @Service} ni {@code @Transactional}. La transacción la pone
 * infraestructura alrededor de este caso de uso ({@code OrderingConfiguration}).
 */
public final class PlaceOrderService implements PlaceOrderUseCase {

  private final OrderRepository orders;
  private final ProductCatalog catalog;
  private final UnpaidOrdersLimit unpaidOrdersLimit;
  private final DomainEventPublisher events;
  private final Clock clock;

  public PlaceOrderService(
      OrderRepository orders,
      ProductCatalog catalog,
      UnpaidOrdersLimit unpaidOrdersLimit,
      DomainEventPublisher events,
      Clock clock) {
    this.orders = requireNonNull(orders);
    this.catalog = requireNonNull(catalog);
    this.unpaidOrdersLimit = requireNonNull(unpaidOrdersLimit);
    this.events = requireNonNull(events);
    this.clock = requireNonNull(clock);
  }

  @Override
  public OrderId place(PlaceOrderCommand command) {
    unpaidOrdersLimit.checkCanPlaceOrder(command.customerId());

    List<OrderLine> lines = command.items().stream().map(this::lineAtCurrentPrice).toList();
    Order order = Order.place(OrderId.newId(), command.customerId(), lines, clock.instant());

    orders.save(order);
    events.publish(order.pullDomainEvents());
    return order.id();
  }

  /** El precio sale del catálogo en este momento y queda congelado en la línea. */
  private OrderLine lineAtCurrentPrice(PlaceOrderCommand.Item item) {
    CatalogProduct product =
        catalog.findById(item.productId()).orElseThrow(() -> new ProductNotFound(item.productId()));
    return new OrderLine(product.id(), product.name(), product.price(), item.quantity());
  }
}
