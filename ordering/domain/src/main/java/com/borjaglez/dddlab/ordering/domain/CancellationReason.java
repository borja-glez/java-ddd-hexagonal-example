package com.borjaglez.dddlab.ordering.domain;

/** Por qué se cancela un pedido, con las palabras de negocio. */
public enum CancellationReason {
  /** El cliente lo ha pedido. */
  CUSTOMER_REQUEST,
  /** Ha pasado el plazo de pago ({@link Order#PAYMENT_DEADLINE}) sin pagar. */
  PAYMENT_TIMEOUT
}
