package com.borjaglez.dddlab.ordering.application.service;

import static java.util.Objects.requireNonNull;

import com.borjaglez.dddlab.ordering.application.exception.OrderNotFound;
import com.borjaglez.dddlab.ordering.application.port.in.GetOrderQuery;
import com.borjaglez.dddlab.ordering.application.port.in.OrderView;
import com.borjaglez.dddlab.ordering.application.port.out.OrderViews;
import com.borjaglez.dddlab.ordering.domain.OrderId;

/** [CQRS · query] Consultar un pedido: va directo al lado de lectura, sin cargar el agregado. */
public final class GetOrderService implements GetOrderQuery {

  private final OrderViews orderViews;

  public GetOrderService(OrderViews orderViews) {
    this.orderViews = requireNonNull(orderViews);
  }

  @Override
  public OrderView get(OrderId orderId) {
    return orderViews.findById(orderId).orElseThrow(() -> new OrderNotFound(orderId));
  }
}
