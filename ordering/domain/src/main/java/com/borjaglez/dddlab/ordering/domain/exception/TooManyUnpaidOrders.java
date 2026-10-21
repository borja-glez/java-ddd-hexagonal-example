package com.borjaglez.dddlab.ordering.domain.exception;

import com.borjaglez.dddlab.ordering.domain.CustomerId;

/** Regla: "un cliente no puede acumular más de N pedidos sin pagar". */
public final class TooManyUnpaidOrders extends DomainException {

  public TooManyUnpaidOrders(CustomerId customerId, int max) {
    super("El cliente " + customerId + " ya tiene " + max + " pedidos sin pagar");
  }
}
