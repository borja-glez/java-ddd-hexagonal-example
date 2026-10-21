package com.borjaglez.dddlab.ordering.domain;

/**
 * [DDD · Value Object] Unidades de un producto en una línea: entre 1 y {@link #MAX}.
 *
 * <p>Sustituye a un {@code int} suelto (primitive obsession): la regla "entre 1 y 99" vive aquí y en
 * ningún otro sitio, y una cantidad inválida no puede llegar al pedido.
 */
public record Quantity(int value) {

  public static final int MAX = 99;

  public Quantity {
    if (value < 1 || value > MAX) {
      throw new IllegalArgumentException(
          "La cantidad debe estar entre 1 y " + MAX + " y es " + value);
    }
  }

  public static Quantity of(int value) {
    return new Quantity(value);
  }

  public Quantity add(Quantity other) {
    return new Quantity(value + other.value);
  }
}
