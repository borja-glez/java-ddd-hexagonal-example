package com.borjaglez.dddlab.ordering.domain;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.borjaglez.dddlab.ordering.domain.exception.TooManyUnpaidOrders;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class UnpaidOrdersLimitTest {

  private static final CustomerId ANA = new CustomerId("ana");

  @Test
  void aCustomerBelowTheLimitCanPlaceAnotherOrder() {
    var limit = new UnpaidOrdersLimit(repositoryWithUnpaidOrders(2));

    assertThatCode(() -> limit.checkCanPlaceOrder(ANA)).doesNotThrowAnyException();
  }

  @Test
  void aCustomerWithThreeUnpaidOrdersCannotPlaceAnother() {
    var limit = new UnpaidOrdersLimit(repositoryWithUnpaidOrders(3));

    assertThatThrownBy(() -> limit.checkCanPlaceOrder(ANA))
        .isInstanceOf(TooManyUnpaidOrders.class);
  }

  /** El servicio solo necesita contar: basta un repositorio de mentira con ese número. */
  private static OrderRepository repositoryWithUnpaidOrders(long unpaid) {
    return new OrderRepository() {
      @Override
      public Optional<Order> findById(OrderId id) {
        return Optional.empty();
      }

      @Override
      public void save(Order order) {}

      @Override
      public long countUnpaidBy(CustomerId customerId) {
        return unpaid;
      }

      @Override
      public List<OrderId> findUnpaidPlacedBefore(Instant moment) {
        return List.of();
      }
    };
  }
}
