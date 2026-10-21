package com.borjaglez.dddlab.ordering.domain;

import static java.util.Objects.requireNonNull;

import java.util.UUID;

/**
 * [DDD · Value Object] La identidad de un pedido.
 *
 * <p>Tipar los ids (en vez de pasar {@code UUID} o {@code String} sueltos) hace que el compilador
 * impida pasar un {@link CustomerId} donde se espera un OrderId.
 */
public record OrderId(UUID value) {

  public OrderId {
    requireNonNull(value, "value");
  }

  public static OrderId newId() {
    return new OrderId(UUID.randomUUID());
  }

  /** Lanza IllegalArgumentException si el texto no es un UUID. */
  public static OrderId of(String value) {
    return new OrderId(UUID.fromString(value));
  }

  @Override
  public String toString() {
    return value.toString();
  }
}
