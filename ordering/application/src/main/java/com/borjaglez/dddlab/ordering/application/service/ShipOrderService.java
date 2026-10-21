package com.borjaglez.dddlab.ordering.application.service;

import static java.util.Objects.requireNonNull;

import com.borjaglez.dddlab.ordering.application.exception.OrderNotFound;
import com.borjaglez.dddlab.ordering.application.port.in.ShipOrderUseCase;
import com.borjaglez.dddlab.ordering.application.port.out.DomainEventPublisher;
import com.borjaglez.dddlab.ordering.domain.Order;
import com.borjaglez.dddlab.ordering.domain.OrderId;
import com.borjaglez.dddlab.ordering.domain.OrderRepository;
import java.time.Clock;

/** [DDD · Application Service] Caso de uso "enviar un pedido". */
public final class ShipOrderService implements ShipOrderUseCase {

  private final OrderRepository orders;
  private final DomainEventPublisher events;
  private final Clock clock;

  public ShipOrderService(OrderRepository orders, DomainEventPublisher events, Clock clock) {
    this.orders = requireNonNull(orders);
    this.events = requireNonNull(events);
    this.clock = requireNonNull(clock);
  }

  @Override
  public void ship(OrderId orderId) {
    Order order = orders.findById(orderId).orElseThrow(() -> new OrderNotFound(orderId));

    order.ship(clock.instant());

    orders.save(order);
    events.publish(order.pullDomainEvents());
  }
}
