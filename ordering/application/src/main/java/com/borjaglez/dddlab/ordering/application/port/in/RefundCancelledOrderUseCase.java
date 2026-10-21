package com.borjaglez.dddlab.ordering.application.port.in;

import com.borjaglez.dddlab.ordering.domain.event.OrderCancelled;

/**
 * [Hexagonal · puerto de entrada] [Event Storming · política] "Cuando se cancela un pedido pagado,
 * se reembolsa".
 *
 * <p>Su adaptador de entrada no es HTTP sino un listener de eventos de dominio: un evento también es
 * una forma de entrar en la aplicación.
 */
public interface RefundCancelledOrderUseCase {

  void refundIfPaid(OrderCancelled event);
}
