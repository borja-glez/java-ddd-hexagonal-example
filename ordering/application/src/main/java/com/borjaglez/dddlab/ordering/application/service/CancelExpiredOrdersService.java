package com.borjaglez.dddlab.ordering.application.service;

import static java.util.Objects.requireNonNull;

import com.borjaglez.dddlab.ordering.application.port.in.CancelExpiredOrdersUseCase;
import com.borjaglez.dddlab.ordering.application.port.in.CancelOrderCommand;
import com.borjaglez.dddlab.ordering.application.port.in.CancelOrderUseCase;
import com.borjaglez.dddlab.ordering.domain.CancellationReason;
import com.borjaglez.dddlab.ordering.domain.Order;
import com.borjaglez.dddlab.ordering.domain.OrderId;
import com.borjaglez.dddlab.ordering.domain.OrderRepository;
import com.borjaglez.dddlab.ordering.domain.exception.DomainException;
import java.time.Clock;
import java.time.Instant;
import java.util.List;

/**
 * [DDD · Application Service] Caso de uso "cancelar los pedidos caducados".
 *
 * <p>No cancela en bloque: delega en {@link CancelOrderUseCase} pedido a pedido. Cada cancelación
 * es su propia transacción (regla de Vernon: una transacción modifica un solo agregado), así que si
 * un pedido falla, los demás se cancelan igual.
 */
public final class CancelExpiredOrdersService implements CancelExpiredOrdersUseCase {

  private final OrderRepository orders;
  private final CancelOrderUseCase cancelOrder;
  private final Clock clock;

  public CancelExpiredOrdersService(
      OrderRepository orders, CancelOrderUseCase cancelOrder, Clock clock) {
    this.orders = requireNonNull(orders);
    this.cancelOrder = requireNonNull(cancelOrder);
    this.clock = requireNonNull(clock);
  }

  @Override
  public int cancelExpiredOrders() {
    Instant deadline = clock.instant().minus(Order.PAYMENT_DEADLINE);
    List<OrderId> expired = orders.findUnpaidPlacedBefore(deadline);

    int cancelled = 0;
    for (OrderId orderId : expired) {
      try {
        cancelOrder.cancel(new CancelOrderCommand(orderId, CancellationReason.PAYMENT_TIMEOUT));
        cancelled++;
      } catch (DomainException alreadyChanged) {
        // Se ha pagado o cancelado mientras tanto: ya no está caducado, se deja como está.
      }
    }
    return cancelled;
  }
}
