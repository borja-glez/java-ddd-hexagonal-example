package com.borjaglez.dddlab.ordering.domain.exception;

/**
 * Se ha intentado romper una regla de negocio de Ordering.
 *
 * <p>Cada subclase lleva el nombre con el que negocio describe la regla ({@code
 * OrderAlreadyShipped}, {@code TooManyUnpaidOrders}): el lenguaje ubicuo llega hasta los errores, y
 * el API REST los devuelve con ese mismo nombre.
 */
public abstract class DomainException extends RuntimeException {

  protected DomainException(String message) {
    super(message);
  }
}
