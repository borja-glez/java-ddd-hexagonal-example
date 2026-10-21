package com.borjaglez.dddlab.ordering.contracts;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * [DDD · Evento de integración] [Context map · Published Language] Se ha realizado un pedido.
 *
 * <p>Es un contrato público entre contextos: estable, versionado en el nombre ({@code V1}) y hecho
 * solo con tipos del JDK. Un cambio incompatible no modifica este record: crea {@code
 * OrderPlacedV2} y se publican los dos mientras queden consumidores de la V1.
 */
public record OrderPlacedV1(
    String orderId,
    String customerId,
    BigDecimal totalAmount,
    String currency,
    Instant occurredAt) {}
