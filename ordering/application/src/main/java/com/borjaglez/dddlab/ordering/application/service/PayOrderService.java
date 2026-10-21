package com.borjaglez.dddlab.ordering.application.service;

import static java.util.Objects.requireNonNull;

import com.borjaglez.dddlab.ordering.application.exception.OrderNotFound;
import com.borjaglez.dddlab.ordering.application.exception.PaymentRejected;
import com.borjaglez.dddlab.ordering.application.port.in.PayOrderUseCase;
import com.borjaglez.dddlab.ordering.application.port.out.DomainEventPublisher;
import com.borjaglez.dddlab.ordering.application.port.out.PaymentGateway;
import com.borjaglez.dddlab.ordering.application.port.out.PaymentResult;
import com.borjaglez.dddlab.ordering.domain.Order;
import com.borjaglez.dddlab.ordering.domain.OrderId;
import com.borjaglez.dddlab.ordering.domain.OrderRepository;
import java.time.Clock;

/**
 * [DDD · Application Service] Caso de uso "pagar un pedido".
 *
 * <p>Antes de cobrar pregunta al pedido si admite el pago, para no cobrar un pedido cancelado. Lo que
 * devuelve la pasarela ya llega traducido por la ACL a {@link PaymentResult}: aquí no hay códigos
 * "00" ni céntimos.
 */
public final class PayOrderService implements PayOrderUseCase {

  private final OrderRepository orders;
  private final PaymentGateway paymentGateway;
  private final DomainEventPublisher events;
  private final Clock clock;

  public PayOrderService(
      OrderRepository orders,
      PaymentGateway paymentGateway,
      DomainEventPublisher events,
      Clock clock) {
    this.orders = requireNonNull(orders);
    this.paymentGateway = requireNonNull(paymentGateway);
    this.events = requireNonNull(events);
    this.clock = requireNonNull(clock);
  }

  @Override
  public void pay(OrderId orderId) {
    Order order = orders.findById(orderId).orElseThrow(() -> new OrderNotFound(orderId));
    order.ensureCanBePaid();

    switch (paymentGateway.charge(orderId, order.total())) {
      case PaymentResult.Approved approved -> order.markAsPaid(approved.paymentId(), clock.instant());
      case PaymentResult.Rejected rejected -> throw new PaymentRejected(orderId, rejected.reason());
    }

    orders.save(order);
    events.publish(order.pullDomainEvents());
  }
}
