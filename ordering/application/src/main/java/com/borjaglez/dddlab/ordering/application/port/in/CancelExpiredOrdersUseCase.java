package com.borjaglez.dddlab.ordering.application.port.in;

/** [Hexagonal · puerto de entrada] Cancelar los pedidos cuyo plazo de pago ha caducado. */
public interface CancelExpiredOrdersUseCase {

  /** Devuelve cuántos pedidos ha cancelado. */
  int cancelExpiredOrders();
}
