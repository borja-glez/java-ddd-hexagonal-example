package com.borjaglez.dddlab.ordering.application.port.in;

import com.borjaglez.dddlab.ordering.domain.OrderId;

/**
 * [Hexagonal · puerto de entrada · lectura] Consultar un pedido.
 *
 * <p>Separado de los comandos (CQRS ligero): leer no tiene reglas que proteger, así que no pasa por
 * el agregado y devuelve directamente una vista pensada para quien la pide.
 */
public interface GetOrderQuery {

  OrderView get(OrderId orderId);
}
