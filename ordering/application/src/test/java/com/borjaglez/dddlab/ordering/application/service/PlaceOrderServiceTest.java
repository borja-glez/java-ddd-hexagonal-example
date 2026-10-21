package com.borjaglez.dddlab.ordering.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.borjaglez.dddlab.ordering.application.exception.ProductNotFound;
import com.borjaglez.dddlab.ordering.application.fakes.InMemoryOrderRepository;
import com.borjaglez.dddlab.ordering.application.fakes.RecordingEventPublisher;
import com.borjaglez.dddlab.ordering.application.fakes.StubProductCatalog;
import com.borjaglez.dddlab.ordering.application.port.in.PlaceOrderCommand;
import com.borjaglez.dddlab.ordering.domain.CustomerId;
import com.borjaglez.dddlab.ordering.domain.Order;
import com.borjaglez.dddlab.ordering.domain.OrderId;
import com.borjaglez.dddlab.ordering.domain.OrderStatus;
import com.borjaglez.dddlab.ordering.domain.ProductId;
import com.borjaglez.dddlab.ordering.domain.Quantity;
import com.borjaglez.dddlab.ordering.domain.UnpaidOrdersLimit;
import com.borjaglez.dddlab.ordering.domain.event.OrderPlaced;
import com.borjaglez.dddlab.ordering.domain.exception.TooManyUnpaidOrders;
import com.borjaglez.dddlab.sharedkernel.Money;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * El caso de uso se prueba con fakes de sus puertos de salida: sin Spring, sin base de datos, sin
 * el módulo Catalog. Es el beneficio práctico de la hexagonal.
 */
class PlaceOrderServiceTest {

  private static final Instant NOW = Instant.parse("2026-10-16T10:00:00Z");
  private static final CustomerId ANA = new CustomerId("ana");

  private final InMemoryOrderRepository orders = new InMemoryOrderRepository();
  private final StubProductCatalog catalog =
      new StubProductCatalog().with("keyboard", "Teclado", "89.90").with("mouse", "Ratón", "29.95");
  private final RecordingEventPublisher events = new RecordingEventPublisher();
  private final PlaceOrderService placeOrder =
      new PlaceOrderService(
          orders,
          catalog,
          new UnpaidOrdersLimit(orders),
          events,
          Clock.fixed(NOW, ZoneOffset.UTC));

  @Test
  void placesAnOrderAtTheCurrentCatalogPricesAndPublishesTheFact() {
    OrderId orderId = placeOrder.place(command(ANA, item("keyboard", 1), item("mouse", 2)));

    Order saved = orders.findById(orderId).orElseThrow();
    assertThat(saved.status()).isEqualTo(OrderStatus.PLACED);
    assertThat(saved.total()).isEqualTo(Money.euros("149.80"));
    assertThat(events.published())
        .containsExactly(new OrderPlaced(orderId, ANA, Money.euros("149.80"), NOW));
  }

  @Test
  void theOrderKeepsThePriceItWasBoughtAt() {
    OrderId orderId = placeOrder.place(command(ANA, item("keyboard", 1)));

    catalog.with("keyboard", "Teclado", "120.00");

    assertThat(orders.findById(orderId).orElseThrow().total()).isEqualTo(Money.euros("89.90"));
  }

  @Test
  void anUnknownProductIsReportedAndNothingIsSaved() {
    assertThatThrownBy(() -> placeOrder.place(command(ANA, item("does-not-exist", 1))))
        .isInstanceOf(ProductNotFound.class);
    assertThat(orders.countUnpaidBy(ANA)).isZero();
    assertThat(events.published()).isEmpty();
  }

  @Test
  void aCustomerCannotPileUpUnpaidOrders() {
    placeOrder.place(command(ANA, item("keyboard", 1)));
    placeOrder.place(command(ANA, item("keyboard", 1)));
    placeOrder.place(command(ANA, item("keyboard", 1)));

    assertThatThrownBy(() -> placeOrder.place(command(ANA, item("keyboard", 1))))
        .isInstanceOf(TooManyUnpaidOrders.class);
  }

  private static PlaceOrderCommand command(CustomerId customer, PlaceOrderCommand.Item... items) {
    return new PlaceOrderCommand(customer, List.of(items));
  }

  private static PlaceOrderCommand.Item item(String productId, int quantity) {
    return new PlaceOrderCommand.Item(new ProductId(productId), Quantity.of(quantity));
  }
}
