package com.borjaglez.dddlab.ordering.domain;

/** [DDD · Value Object] Referencia a un producto del catálogo, solo por su id. */
public record ProductId(String value) {

  public ProductId {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("El id de producto es obligatorio");
    }
  }

  @Override
  public String toString() {
    return value;
  }
}
