package com.borjaglez.dddlab.ordering.contracts;

import java.time.Instant;

/** [DDD · Evento de integración] Se ha enviado un pedido. */
public record OrderShippedV1(String orderId, String customerId, Instant occurredAt) {}
