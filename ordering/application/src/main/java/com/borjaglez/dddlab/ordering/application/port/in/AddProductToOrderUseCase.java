package com.borjaglez.dddlab.ordering.application.port.in;

/** [Hexagonal · puerto de entrada] Añadir un producto a un pedido que aún no se ha pagado. */
public interface AddProductToOrderUseCase {

  void addProduct(AddProductToOrderCommand command);
}
