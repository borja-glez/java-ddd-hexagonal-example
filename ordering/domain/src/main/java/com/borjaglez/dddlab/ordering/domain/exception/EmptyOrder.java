package com.borjaglez.dddlab.ordering.domain.exception;

/** Regla: "un pedido tiene al menos una línea". */
public final class EmptyOrder extends DomainException {

  public EmptyOrder() {
    super("Un pedido necesita al menos un producto");
  }
}
