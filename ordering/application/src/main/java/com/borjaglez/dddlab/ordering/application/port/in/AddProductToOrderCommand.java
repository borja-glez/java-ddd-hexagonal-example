package com.borjaglez.dddlab.ordering.application.port.in;

import static java.util.Objects.requireNonNull;

import com.borjaglez.dddlab.ordering.domain.OrderId;
import com.borjaglez.dddlab.ordering.domain.ProductId;
import com.borjaglez.dddlab.ordering.domain.Quantity;

public record AddProductToOrderCommand(OrderId orderId, ProductId productId, Quantity quantity) {

  public AddProductToOrderCommand {
    requireNonNull(orderId, "orderId");
    requireNonNull(productId, "productId");
    requireNonNull(quantity, "quantity");
  }
}
