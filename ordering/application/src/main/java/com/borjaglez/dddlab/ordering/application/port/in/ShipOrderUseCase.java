package com.borjaglez.dddlab.ordering.application.port.in;

import com.borjaglez.dddlab.ordering.domain.OrderId;

/** [Hexagonal · puerto de entrada] Enviar un pedido pagado. */
public interface ShipOrderUseCase {

  void ship(OrderId orderId);
}
