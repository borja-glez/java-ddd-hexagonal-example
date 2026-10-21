package com.borjaglez.dddlab.ordering.contracts;

import java.time.Instant;

/**
 * [DDD · Evento de integración] Se ha cancelado un pedido.
 *
 * @param reason {@code CUSTOMER_REQUEST} o {@code PAYMENT_TIMEOUT}, como texto: el consumidor no
 *     depende del enum del dominio.
 * @param refundIssued si el pedido estaba pagado y Ordering ha pedido su reembolso.
 */
public record OrderCancelledV1(
    String orderId, String customerId, String reason, boolean refundIssued, Instant occurredAt) {}
