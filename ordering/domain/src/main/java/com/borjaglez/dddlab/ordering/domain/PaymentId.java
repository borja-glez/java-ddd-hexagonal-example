package com.borjaglez.dddlab.ordering.domain;

/**
 * [DDD · Value Object] Referencia del cobro de un pedido.
 *
 * <p>El valor lo genera la pasarela, pero el tipo es nuestro: el dominio no sabe qué pasarela hay
 * detrás ni que en LegacyPay se llama {@code txnId}.
 */
public record PaymentId(String value) {

  public PaymentId {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("La referencia de pago es obligatoria");
    }
  }

  @Override
  public String toString() {
    return value;
  }
}
