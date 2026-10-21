package com.borjaglez.dddlab.ordering.infrastructure.in.rest;

import com.borjaglez.dddlab.ordering.application.exception.NotFoundException;
import com.borjaglez.dddlab.ordering.application.exception.PaymentRejected;
import com.borjaglez.dddlab.ordering.domain.exception.DomainException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Traduce los errores del núcleo a HTTP. El núcleo lanza excepciones con nombres de negocio; saber
 * que {@code OrderAlreadyShipped} es un 409 es cosa del adaptador REST, no del dominio.
 *
 * <p>El {@code title} de la respuesta es el nombre de la regla, así que el lenguaje ubicuo llega
 * hasta quien consume el API.
 */
@RestControllerAdvice(assignableTypes = OrderController.class)
class OrderingExceptionHandler {

  /** Una regla de negocio impide la operación en el estado actual del pedido. */
  @ExceptionHandler(DomainException.class)
  ProblemDetail onBusinessRuleViolation(DomainException exception) {
    return problem(HttpStatus.CONFLICT, exception);
  }

  @ExceptionHandler(NotFoundException.class)
  ProblemDetail onNotFound(NotFoundException exception) {
    return problem(HttpStatus.NOT_FOUND, exception);
  }

  @ExceptionHandler(PaymentRejected.class)
  ProblemDetail onPaymentRejected(PaymentRejected exception) {
    return problem(HttpStatus.PAYMENT_REQUIRED, exception);
  }

  /** Un value object ha rechazado el dato de entrada (cantidad 0, id vacío, UUID mal formado). */
  @ExceptionHandler(IllegalArgumentException.class)
  ProblemDetail onInvalidInput(IllegalArgumentException exception) {
    return problem(HttpStatus.BAD_REQUEST, "InvalidInput", exception);
  }

  private static ProblemDetail problem(HttpStatus status, Exception exception) {
    return problem(status, exception.getClass().getSimpleName(), exception);
  }

  private static ProblemDetail problem(HttpStatus status, String title, Exception exception) {
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, exception.getMessage());
    problem.setTitle(title);
    return problem;
  }
}
