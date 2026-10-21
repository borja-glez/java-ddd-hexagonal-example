package com.borjaglez.dddlab.ordering.domain.exception;

import com.borjaglez.dddlab.ordering.domain.OrderId;
import com.borjaglez.dddlab.ordering.domain.OrderStatus;

/** Regla: "solo se añaden productos a un pedido que aún no se ha pagado". */
public final class OrderCannotBeModified extends DomainException {

  public OrderCannotBeModified(OrderId orderId, OrderStatus current) {
    super("El pedido " + orderId + " está " + current + " y ya no admite cambios");
  }
}
