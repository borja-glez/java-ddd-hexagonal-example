package com.borjaglez.dddlab.ordering.domain.exception;

import com.borjaglez.dddlab.ordering.domain.OrderId;

/** Regla: "un pedido enviado no se puede cancelar". */
public final class OrderAlreadyShipped extends DomainException {

  public OrderAlreadyShipped(OrderId orderId) {
    super("El pedido " + orderId + " ya se ha enviado y no se puede cancelar");
  }
}
