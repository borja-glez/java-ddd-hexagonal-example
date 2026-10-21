package com.borjaglez.dddlab.ordering.application.port.out;

import com.borjaglez.dddlab.ordering.domain.PaymentId;

/** El resultado de un cobro, en términos de Ordering: aprobado (con su referencia) o rechazado. */
public sealed interface PaymentResult {

  record Approved(PaymentId paymentId) implements PaymentResult {}

  record Rejected(String reason) implements PaymentResult {}
}
