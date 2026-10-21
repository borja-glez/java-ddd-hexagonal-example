package com.borjaglez.dddlab.ordering.domain.event;

import com.borjaglez.dddlab.ordering.domain.CustomerId;
import com.borjaglez.dddlab.ordering.domain.OrderId;
import com.borjaglez.dddlab.sharedkernel.Money;
import java.time.Instant;

/** [DDD · Domain Event] Se ha realizado un pedido. */
public record OrderPlaced(OrderId orderId, CustomerId customerId, Money total, Instant occurredAt)
    implements DomainEvent {}
