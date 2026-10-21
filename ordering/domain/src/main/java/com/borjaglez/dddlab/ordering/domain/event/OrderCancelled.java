package com.borjaglez.dddlab.ordering.domain.event;

import com.borjaglez.dddlab.ordering.domain.CancellationReason;
import com.borjaglez.dddlab.ordering.domain.CustomerId;
import com.borjaglez.dddlab.ordering.domain.OrderId;
import com.borjaglez.dddlab.ordering.domain.PaymentId;
import com.borjaglez.dddlab.sharedkernel.Money;
import java.time.Instant;

/**
 * [DDD · Domain Event] Se ha cancelado un pedido.
 *
 * @param paymentId el cobro del pedido, o {@code null} si no llegó a pagarse.
 * @param amount el total del pedido, lo que hay que devolver si estaba pagado.
 */
public record OrderCancelled(
    OrderId orderId,
    CustomerId customerId,
    CancellationReason reason,
    PaymentId paymentId,
    Money amount,
    Instant occurredAt)
    implements DomainEvent {

  public boolean wasPaid() {
    return paymentId != null;
  }
}
