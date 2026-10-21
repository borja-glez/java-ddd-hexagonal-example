package com.borjaglez.dddlab.ordering.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.borjaglez.dddlab.ordering.application.fakes.FakePaymentGateway;
import com.borjaglez.dddlab.ordering.application.fakes.InMemoryOrderRepository;
import com.borjaglez.dddlab.ordering.application.fakes.RecordingEventPublisher;
import com.borjaglez.dddlab.ordering.application.port.in.CancelOrderCommand;
import com.borjaglez.dddlab.ordering.domain.CancellationReason;
import com.borjaglez.dddlab.ordering.domain.CustomerId;
import com.borjaglez.dddlab.ordering.domain.Order;
import com.borjaglez.dddlab.ordering.domain.OrderId;
import com.borjaglez.dddlab.ordering.domain.OrderLine;
import com.borjaglez.dddlab.ordering.domain.OrderStatus;
import com.borjaglez.dddlab.ordering.domain.PaymentId;
import com.borjaglez.dddlab.ordering.domain.ProductId;
import com.borjaglez.dddlab.ordering.domain.Quantity;
import com.borjaglez.dddlab.ordering.domain.event.OrderCancelled;
import com.borjaglez.dddlab.sharedkernel.Money;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Cancelar, caducar y reembolsar: tres casos de uso que colaboran a través de un evento. El reloj es
 * un {@link Clock} fijo, así que "han pasado 31 minutos" se prueba sin esperar 31 minutos.
 */
class CancellationTest {

  private static final Instant NOW = Instant.parse("2026-10-16T10:00:00Z");

  private final InMemoryOrderRepository orders = new InMemoryOrderRepository();
  private final RecordingEventPublisher events = new RecordingEventPublisher();
  private final Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);
  private final CancelOrderService cancelOrder = new CancelOrderService(orders, events, clock);

  @Test
  void cancelsAnOrderAndPublishesWhyItWasCancelled() {
    OrderId orderId = orderPlacedAt(NOW);

    cancelOrder.cancel(new CancelOrderCommand(orderId, CancellationReason.CUSTOMER_REQUEST));

    assertThat(orders.findById(orderId).orElseThrow().status()).isEqualTo(OrderStatus.CANCELLED);
    assertThat(events.published())
        .singleElement()
        .isInstanceOfSatisfying(
            OrderCancelled.class,
            cancelled -> assertThat(cancelled.reason()).isEqualTo(CancellationReason.CUSTOMER_REQUEST));
  }

  @Test
  void unpaidOrdersOlderThanTheDeadlineExpireAndRecentOnesStay() {
    OrderId expired = orderPlacedAt(NOW.minus(Duration.ofMinutes(31)));
    OrderId recent = orderPlacedAt(NOW.minus(Duration.ofMinutes(5)));
    var cancelExpired = new CancelExpiredOrdersService(orders, cancelOrder, clock);

    int cancelled = cancelExpired.cancelExpiredOrders();

    assertThat(cancelled).isEqualTo(1);
    assertThat(orders.findById(expired).orElseThrow().status()).isEqualTo(OrderStatus.CANCELLED);
    assertThat(orders.findById(recent).orElseThrow().status()).isEqualTo(OrderStatus.PLACED);
  }

  @Test
  void theRefundPolicyRefundsOnlyOrdersThatWerePaid() {
    var paymentGateway = new FakePaymentGateway();
    var refundPolicy = new RefundCancelledOrderService(paymentGateway);
    Money total = Money.euros("89.90");

    refundPolicy.refundIfPaid(cancelled(new PaymentId("LP-1"), total));
    refundPolicy.refundIfPaid(cancelled(null, total));

    assertThat(paymentGateway.refunds())
        .containsExactly(new FakePaymentGateway.Refund(new PaymentId("LP-1"), total));
  }

  private OrderId orderPlacedAt(Instant placedAt) {
    OrderLine keyboard =
        new OrderLine(new ProductId("keyboard"), "Teclado", Money.euros("89.90"), Quantity.of(1));
    Order order = Order.place(OrderId.newId(), new CustomerId("ana"), List.of(keyboard), placedAt);
    orders.save(order);
    return order.id();
  }

  private static OrderCancelled cancelled(PaymentId paymentId, Money amount) {
    return new OrderCancelled(
        OrderId.newId(),
        new CustomerId("ana"),
        CancellationReason.CUSTOMER_REQUEST,
        paymentId,
        amount,
        NOW);
  }
}
