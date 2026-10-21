package com.borjaglez.dddlab.ordering.infrastructure.in.events;

import com.borjaglez.dddlab.ordering.application.port.in.RefundCancelledOrderUseCase;
import com.borjaglez.dddlab.ordering.domain.event.OrderCancelled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * [Hexagonal · adaptador de entrada (driving)] Escucha el evento de dominio {@link OrderCancelled}
 * y dispara la política de reembolso.
 *
 * <p>{@code @TransactionalEventListener} se ejecuta por defecto después del commit: solo se
 * reembolsa si la cancelación ha quedado guardada de verdad (consistencia eventual entre "pedido
 * cancelado" y "dinero devuelto").
 */
@Component
class OrderCancelledListener {

  private final RefundCancelledOrderUseCase refundCancelledOrder;

  OrderCancelledListener(RefundCancelledOrderUseCase refundCancelledOrder) {
    this.refundCancelledOrder = refundCancelledOrder;
  }

  @TransactionalEventListener
  void on(OrderCancelled event) {
    refundCancelledOrder.refundIfPaid(event);
  }
}
