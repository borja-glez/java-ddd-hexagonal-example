package com.borjaglez.dddlab.ordering.domain.event;

import com.borjaglez.dddlab.ordering.domain.CustomerId;
import com.borjaglez.dddlab.ordering.domain.OrderId;
import java.time.Instant;

/** [DDD · Domain Event] Se ha enviado un pedido. */
public record OrderShipped(OrderId orderId, CustomerId customerId, Instant occurredAt)
    implements DomainEvent {}
