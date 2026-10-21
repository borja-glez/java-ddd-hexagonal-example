package com.borjaglez.dddlab.ordering.application.service;

import static java.util.Objects.requireNonNull;

import com.borjaglez.dddlab.ordering.application.port.in.RefundCancelledOrderUseCase;
import com.borjaglez.dddlab.ordering.application.port.out.PaymentGateway;
import com.borjaglez.dddlab.ordering.domain.event.OrderCancelled;

/**
 * [Event Storming · política] "Cuando se cancela un pedido pagado, se reembolsa el importe".
 *
 * <p>Reacciona a un hecho ya ocurrido. Añadir otra reacción (liberar stock, avisar a contabilidad)
 * es escribir otra política como esta, sin tocar {@code Order} ni {@code CancelOrderService}.
 */
public final class RefundCancelledOrderService implements RefundCancelledOrderUseCase {

  private final PaymentGateway paymentGateway;

  public RefundCancelledOrderService(PaymentGateway paymentGateway) {
    this.paymentGateway = requireNonNull(paymentGateway);
  }

  @Override
  public void refundIfPaid(OrderCancelled event) {
    if (event.wasPaid()) {
      paymentGateway.refund(event.paymentId(), event.amount());
    }
  }
}
