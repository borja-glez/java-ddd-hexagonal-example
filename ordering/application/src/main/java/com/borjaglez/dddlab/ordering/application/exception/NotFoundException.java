package com.borjaglez.dddlab.ordering.application.exception;

/** Algo que el caso de uso necesita no existe. No es una regla de negocio rota: es un dato que falta. */
public abstract class NotFoundException extends RuntimeException {

  protected NotFoundException(String message) {
    super(message);
  }
}
