package com.borjaglez.dddlab.ordering.domain.exception;

/** Regla: "un pedido tiene como mucho N productos distintos". */
public final class TooManyLines extends DomainException {

  public TooManyLines(int max) {
    super("Un pedido admite como mucho " + max + " productos distintos");
  }
}
