package com.borjaglez.dddlab.ordering.application.port.in;

/**
 * [Hexagonal · puerto de entrada] Cancelar un pedido.
 *
 * <p>Lo usan dos adaptadores de entrada distintos: el controlador REST (el cliente cancela) y el
 * job programado (caduca el plazo de pago). La regla está una sola vez, en {@code Order.cancel}.
 */
public interface CancelOrderUseCase {

  void cancel(CancelOrderCommand command);
}
