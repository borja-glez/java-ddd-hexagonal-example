package com.borjaglez.dddlab.ordering.application.service;

import static java.util.Objects.requireNonNull;

import com.borjaglez.dddlab.ordering.application.exception.OrderNotFound;
import com.borjaglez.dddlab.ordering.application.port.in.CancelOrderCommand;
import com.borjaglez.dddlab.ordering.application.port.in.CancelOrderUseCase;
import com.borjaglez.dddlab.ordering.application.port.out.DomainEventPublisher;
import com.borjaglez.dddlab.ordering.domain.Order;
import com.borjaglez.dddlab.ordering.domain.OrderRepository;
import java.time.Clock;

/**
 * [DDD · Application Service] Caso de uso "cancelar un pedido".
 *
 * <p>No reembolsa nada: el pedido anota {@code OrderCancelled} y la política de reembolso reacciona
 * después. Cancelar y reembolsar son dos pasos desacoplados por un evento.
 */
public final class CancelOrderService implements CancelOrderUseCase {

  private final OrderRepository orders;
  private final DomainEventPublisher events;
  private final Clock clock;

  public CancelOrderService(OrderRepository orders, DomainEventPublisher events, Clock clock) {
    this.orders = requireNonNull(orders);
    this.events = requireNonNull(events);
    this.clock = requireNonNull(clock);
  }

  @Override
  public void cancel(CancelOrderCommand command) {
    Order order =
        orders.findById(command.orderId()).orElseThrow(() -> new OrderNotFound(command.orderId()));

    order.cancel(command.reason(), clock.instant());

    orders.save(order);
    events.publish(order.pullDomainEvents());
  }
}
