package com.borjaglez.dddlab.ordering.domain.event;

import com.borjaglez.dddlab.ordering.domain.CustomerId;
import com.borjaglez.dddlab.ordering.domain.OrderId;
import com.borjaglez.dddlab.ordering.domain.PaymentId;
import com.borjaglez.dddlab.sharedkernel.Money;
import java.time.Instant;

/** [DDD · Domain Event] Se ha cobrado un pedido. */
public record OrderPaid(
    OrderId orderId, CustomerId customerId, PaymentId paymentId, Money amount, Instant occurredAt)
    implements DomainEvent {}
