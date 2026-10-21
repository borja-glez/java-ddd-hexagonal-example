package com.borjaglez.dddlab.ordering.contracts;

import java.math.BigDecimal;
import java.time.Instant;

/** [DDD · Evento de integración] Se ha cobrado un pedido. */
public record OrderPaidV1(
    String orderId, String customerId, BigDecimal amount, String currency, Instant occurredAt) {}
