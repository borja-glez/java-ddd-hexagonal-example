package com.borjaglez.dddlab.ordering.application.exception;

import com.borjaglez.dddlab.ordering.domain.OrderId;

/** La pasarela no ha aprobado el cobro. El pedido sigue sin pagar y se puede reintentar. */
public final class PaymentRejected extends RuntimeException {

  public PaymentRejected(OrderId orderId, String reason) {
    super("No se ha podido cobrar el pedido " + orderId + ": " + reason);
  }
}
