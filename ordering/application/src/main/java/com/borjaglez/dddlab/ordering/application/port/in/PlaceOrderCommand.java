package com.borjaglez.dddlab.ordering.application.port.in;

import static java.util.Objects.requireNonNull;

import com.borjaglez.dddlab.ordering.domain.CustomerId;
import com.borjaglez.dddlab.ordering.domain.ProductId;
import com.borjaglez.dddlab.ordering.domain.Quantity;
import java.util.List;

/**
 * Lo que pide el cliente: qué productos y cuántos. Ya viene en value objects, así que si llega
 * aquí, los datos son válidos. El precio no viaja en el comando: lo pone el catálogo.
 */
public record PlaceOrderCommand(CustomerId customerId, List<Item> items) {

  public PlaceOrderCommand {
    requireNonNull(customerId, "customerId");
    items = List.copyOf(items);
  }

  public record Item(ProductId productId, Quantity quantity) {

    public Item {
      requireNonNull(productId, "productId");
      requireNonNull(quantity, "quantity");
    }
  }
}
