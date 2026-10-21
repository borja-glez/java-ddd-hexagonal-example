package com.borjaglez.dddlab.ordering.domain;

/**
 * [DDD · Value Object] Referencia a un cliente.
 *
 * <p>El cliente pertenece a otro contexto (clientes, autenticación): Ordering solo guarda su id.
 * Es la regla de Vernon "referencia a otros agregados solo por id, nunca por objeto".
 */
public record CustomerId(String value) {

  public CustomerId {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("El id de cliente es obligatorio");
    }
  }

  @Override
  public String toString() {
    return value;
  }
}
