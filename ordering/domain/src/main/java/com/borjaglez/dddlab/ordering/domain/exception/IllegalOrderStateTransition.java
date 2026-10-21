package com.borjaglez.dddlab.ordering.domain.exception;

import com.borjaglez.dddlab.ordering.domain.OrderId;
import com.borjaglez.dddlab.ordering.domain.OrderStatus;

/** Regla: el pedido solo cambia de estado siguiendo el ciclo de vida de {@link OrderStatus}. */
public final class IllegalOrderStateTransition extends DomainException {

  public IllegalOrderStateTransition(OrderId orderId, OrderStatus current, String action) {
    super("No se puede " + action + " el pedido " + orderId + " porque está " + current);
  }
}
