package com.borjaglez.dddlab.ordering.domain;

import static java.util.Objects.requireNonNull;

import com.borjaglez.dddlab.ordering.domain.exception.TooManyUnpaidOrders;

/**
 * [DDD · Domain Service] Regla de negocio: un cliente no puede acumular más de {@link
 * #MAX_UNPAID_ORDERS} pedidos sin pagar.
 *
 * <p>No cabe dentro de {@link Order}: un pedido no sabe nada de los otros pedidos del cliente, y la
 * regla mira varios agregados a la vez. Por eso es un servicio de dominio: lógica de negocio pura,
 * sin estado propio, que solo depende de un puerto del dominio ({@link OrderRepository}).
 */
public final class UnpaidOrdersLimit {

  public static final int MAX_UNPAID_ORDERS = 3;

  private final OrderRepository orders;

  public UnpaidOrdersLimit(OrderRepository orders) {
    this.orders = requireNonNull(orders, "orders");
  }

  public void checkCanPlaceOrder(CustomerId customerId) {
    if (orders.countUnpaidBy(customerId) >= MAX_UNPAID_ORDERS) {
      throw new TooManyUnpaidOrders(customerId, MAX_UNPAID_ORDERS);
    }
  }
}
