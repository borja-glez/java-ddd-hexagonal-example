package com.borjaglez.dddlab.ordering.application.exception;

import com.borjaglez.dddlab.ordering.domain.OrderId;

public final class OrderNotFound extends NotFoundException {

  public OrderNotFound(OrderId orderId) {
    super("No existe el pedido " + orderId);
  }
}
