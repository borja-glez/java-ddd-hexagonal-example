package com.borjaglez.dddlab.ordering.application.port.in;

import com.borjaglez.dddlab.ordering.domain.OrderId;

/**
 * [Hexagonal · puerto de entrada] Realizar un pedido.
 *
 * <p>Es lo que la aplicación ofrece al exterior, definido por el núcleo y en su idioma. El
 * controlador REST no sabe qué clase lo implementa, ni si hay una transacción alrededor.
 */
public interface PlaceOrderUseCase {

  OrderId place(PlaceOrderCommand command);
}
