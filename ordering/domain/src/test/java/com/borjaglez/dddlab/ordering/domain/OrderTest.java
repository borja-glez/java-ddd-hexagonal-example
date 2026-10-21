package com.borjaglez.dddlab.ordering.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
import java.time.Instant;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Las reglas de negocio se prueban con {@code new}: sin Spring, sin base de datos, sin mocks. Cada
 * test tarda milisegundos y se lee como la regla que comprueba.
 */
class OrderTest {

  private static final Instant NOW = Instant.parse("2026-10-16T10:00:00Z");
  private static final CustomerId ANA = new CustomerId("ana");

  @Nested
  class Placing {

    @Test
    void placesAnOrderWithItsLinesAndAnnouncesIt() {
      Order order = placeOrder(keyboard(1), mouse(2));

      assertThat(order.status()).isEqualTo(OrderStatus.PLACED);
      assertThat(order.total()).isEqualTo(Money.euros("149.80"));
      assertThat(order.pullDomainEvents())
          .containsExactly(new OrderPlaced(order.id(), ANA, Money.euros("149.80"), NOW));
    }

    @Test
    void anOrderNeedsAtLeastOneLine() {
      assertThatThrownBy(() -> Order.place(OrderId.newId(), ANA, List.of(), NOW))
          .isInstanceOf(EmptyOrder.class);
    }

    @Test
    void theSameProductTwiceIsOneLineWithBothQuantities() {
      Order order = placeOrder(mouse(1), mouse(2));

      assertThat(order.lines()).singleElement().extracting(OrderLine::quantity)
          .isEqualTo(Quantity.of(3));
    }

    @Test
    void anOrderHasAtMostTenDifferentProducts() {
      List<OrderLine> elevenProducts =
          IntStream.rangeClosed(1, 11)
              .mapToObj(i -> line("product-" + i, "1.00", 1))
              .toList();

      assertThatThrownBy(() -> Order.place(OrderId.newId(), ANA, elevenProducts, NOW))
          .isInstanceOf(TooManyLines.class);
    }

    @Test
    void reconstitutingAnOrderIsNotANewFactAndAnnouncesNothing() {
      Order loaded =
          Order.reconstitute(
              OrderId.newId(), ANA, List.of(keyboard(1)), NOW, OrderStatus.PAID, new PaymentId("LP-1"));

      assertThat(loaded.pullDomainEvents()).isEmpty();
    }
  }

  @Nested
  class Modifying {

    @Test
    void addsProductsWhileTheOrderIsNotPaid() {
      Order order = placeOrder(keyboard(1));

      order.addLine(mouse(1));

      assertThat(order.lines()).hasSize(2);
      assertThat(order.total()).isEqualTo(Money.euros("119.85"));
    }

    @Test
    void aPaidOrderCannotBeModified() {
      Order order = paidOrder();

      assertThatThrownBy(() -> order.addLine(mouse(1))).isInstanceOf(OrderCannotBeModified.class);
    }

    @Test
    void theLinesSeenFromOutsideCannotBeChanged() {
      Order order = placeOrder(keyboard(1));

      assertThatThrownBy(() -> order.lines().add(mouse(1)))
          .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void keepsThePriceFrozenWhenTheSameProductIsAddedAgain() {
      Order order = placeOrder(line("keyboard", "89.90", 1));

      order.addLine(line("keyboard", "99.90", 1));

      assertThat(order.total()).isEqualTo(Money.euros("179.80"));
    }
  }

  @Nested
  class Lifecycle {

    @Test
    void aPlacedOrderIsPaidAndThenShipped() {
      Order order = placeOrder(keyboard(1));
      order.pullDomainEvents();

      order.markAsPaid(new PaymentId("LP-1"), NOW);
      order.ship(NOW);

      assertThat(order.status()).isEqualTo(OrderStatus.SHIPPED);
      assertThat(order.pullDomainEvents())
          .containsExactly(
              new OrderPaid(order.id(), ANA, new PaymentId("LP-1"), Money.euros("89.90"), NOW),
              new OrderShipped(order.id(), ANA, NOW));
    }

    @Test
    void anOrderCannotBeShippedBeforeItIsPaid() {
      Order order = placeOrder(keyboard(1));

      assertThatThrownBy(() -> order.ship(NOW)).isInstanceOf(IllegalOrderStateTransition.class);
    }

    @Test
    void aCancelledOrderCannotBePaid() {
      Order order = placeOrder(keyboard(1));
      order.cancel(CancellationReason.CUSTOMER_REQUEST, NOW);

      assertThatThrownBy(order::ensureCanBePaid).isInstanceOf(IllegalOrderStateTransition.class);
    }
  }

  @Nested
  class Cancelling {

    @Test
    void aShippedOrderCannotBeCancelled() {
      Order order = paidOrder();
      order.ship(NOW);

      assertThatThrownBy(() -> order.cancel(CancellationReason.CUSTOMER_REQUEST, NOW))
          .isInstanceOf(OrderAlreadyShipped.class);
    }

    @Test
    void cancellingAPaidOrderAnnouncesThatItWasPaidSoItGetsRefunded() {
      Order order = paidOrder();
      order.pullDomainEvents();

      order.cancel(CancellationReason.CUSTOMER_REQUEST, NOW);

      assertThat(order.pullDomainEvents())
          .singleElement()
          .isInstanceOfSatisfying(
              OrderCancelled.class,
              cancelled -> {
                assertThat(cancelled.wasPaid()).isTrue();
                assertThat(cancelled.amount()).isEqualTo(Money.euros("89.90"));
              });
    }

    @Test
    void cancellingAnUnpaidOrderNeedsNoRefund() {
      Order order = placeOrder(keyboard(1));
      order.pullDomainEvents();

      order.cancel(CancellationReason.PAYMENT_TIMEOUT, NOW);

      assertThat(order.pullDomainEvents())
          .singleElement()
          .isInstanceOfSatisfying(
              OrderCancelled.class, cancelled -> assertThat(cancelled.wasPaid()).isFalse());
    }

    @Test
    void anOrderIsCancelledOnlyOnce() {
      Order order = placeOrder(keyboard(1));
      order.cancel(CancellationReason.CUSTOMER_REQUEST, NOW);

      assertThatThrownBy(() -> order.cancel(CancellationReason.CUSTOMER_REQUEST, NOW))
          .isInstanceOf(IllegalOrderStateTransition.class);
    }
  }

  @Test
  void twoOrdersWithTheSameDataAreStillTwoOrders() {
    assertThat(placeOrder(keyboard(1))).isNotEqualTo(placeOrder(keyboard(1)));
  }

  private static Order placeOrder(OrderLine... lines) {
    return Order.place(OrderId.newId(), ANA, List.of(lines), NOW);
  }

  private static Order paidOrder() {
    Order order = placeOrder(keyboard(1));
    order.markAsPaid(new PaymentId("LP-1"), NOW);
    return order;
  }

  private static OrderLine keyboard(int quantity) {
    return line("keyboard", "89.90", quantity);
  }

  private static OrderLine mouse(int quantity) {
    return line("mouse", "29.95", quantity);
  }

  private static OrderLine line(String productId, String price, int quantity) {
    return new OrderLine(
        new ProductId(productId), productId, Money.euros(price), Quantity.of(quantity));
  }
}
