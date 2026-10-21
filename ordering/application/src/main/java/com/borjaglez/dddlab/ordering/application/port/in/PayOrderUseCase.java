package com.borjaglez.dddlab.ordering.application.port.in;

import com.borjaglez.dddlab.ordering.domain.OrderId;

/** [Hexagonal · puerto de entrada] Pagar un pedido. */
public interface PayOrderUseCase {

  void pay(OrderId orderId);
}
