package com.borjaglez.dddlab.ordering.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.borjaglez.dddlab.ordering.application.exception.PaymentRejected;
import com.borjaglez.dddlab.ordering.application.fakes.FakePaymentGateway;
import com.borjaglez.dddlab.ordering.application.fakes.InMemoryOrderRepository;
import com.borjaglez.dddlab.ordering.application.fakes.RecordingEventPublisher;
import com.borjaglez.dddlab.ordering.domain.CancellationReason;
import com.borjaglez.dddlab.ordering.domain.CustomerId;
import com.borjaglez.dddlab.ordering.domain.Order;
import com.borjaglez.dddlab.ordering.domain.OrderId;
import com.borjaglez.dddlab.ordering.domain.OrderLine;
import com.borjaglez.dddlab.ordering.domain.OrderStatus;
import com.borjaglez.dddlab.ordering.domain.PaymentId;
import com.borjaglez.dddlab.ordering.domain.ProductId;
import com.borjaglez.dddlab.ordering.domain.Quantity;
import com.borjaglez.dddlab.ordering.domain.event.OrderPaid;
import com.borjaglez.dddlab.ordering.domain.exception.IllegalOrderStateTransition;
import com.borjaglez.dddlab.sharedkernel.Money;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.Test;

class PayOrderServiceTest {

  private static final Instant NOW = Instant.parse("2026-10-16T10:00:00Z");

  private final InMemoryOrderRepository orders = new InMemoryOrderRepository();
  private final FakePaymentGateway paymentGateway = new FakePaymentGateway();
  private final RecordingEventPublisher events = new RecordingEventPublisher();
  private final PayOrderService payOrder =
      new PayOrderService(orders, paymentGateway, events, Clock.fixed(NOW, ZoneOffset.UTC));

  @Test
  void chargesTheOrderTotalAndMarksTheOrderAsPaid() {
    OrderId orderId = existingOrder();

    payOrder.pay(orderId);

    Order paid = orders.findById(orderId).orElseThrow();
    assertThat(paid.status()).isEqualTo(OrderStatus.PAID);
    assertThat(paymentGateway.charges())
        .containsExactly(new FakePaymentGateway.Charge(orderId, Money.euros("89.90")));
    assertThat(events.published()).singleElement().isInstanceOf(OrderPaid.class);
  }

  @Test
  void aRejectedPaymentLeavesTheOrderUnpaid() {
    OrderId orderId = existingOrder();
    paymentGateway.rejectEverythingBecause("Fondos insuficientes");

    assertThatThrownBy(() -> payOrder.pay(orderId))
        .isInstanceOf(PaymentRejected.class)
        .hasMessageContaining("Fondos insuficientes");
    assertThat(orders.findById(orderId).orElseThrow().status()).isEqualTo(OrderStatus.PLACED);
    assertThat(events.published()).isEmpty();
  }

  @Test
  void aCancelledOrderIsNeverCharged() {
    OrderId orderId = existingOrder();
    Order order = orders.findById(orderId).orElseThrow();
    order.cancel(CancellationReason.CUSTOMER_REQUEST, NOW);
    orders.save(order);

    assertThatThrownBy(() -> payOrder.pay(orderId))
        .isInstanceOf(IllegalOrderStateTransition.class);
    assertThat(paymentGateway.charges()).isEmpty();
  }

  @Test
  void keepsThePaymentReferenceTheGatewayReturns() {
    OrderId orderId = existingOrder();

    payOrder.pay(orderId);

    assertThat(orders.findById(orderId).orElseThrow().paymentId())
        .contains(new PaymentId("PAY-1"));
  }

  private OrderId existingOrder() {
    OrderLine keyboard =
        new OrderLine(new ProductId("keyboard"), "Teclado", Money.euros("89.90"), Quantity.of(1));
    Order order = Order.place(OrderId.newId(), new CustomerId("ana"), List.of(keyboard), NOW);
    orders.save(order);
    return order.id();
  }
}
