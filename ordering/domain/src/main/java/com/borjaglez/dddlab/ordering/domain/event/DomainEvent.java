package com.borjaglez.dddlab.ordering.domain.event;

import com.borjaglez.dddlab.ordering.domain.OrderId;
import java.time.Instant;

/**
 * [DDD · Domain Event] Un hecho de negocio que ya ocurrió, con nombre en pasado.
 *
 * <p>Es interno a Ordering, así que puede llevar tipos del dominio ({@code OrderId}, {@code Money}).
 * Lo que sale hacia otros contextos son los eventos de integración del módulo {@code contracts}.
 *
 * <p>Es {@code sealed}: la lista de hechos posibles es cerrada y el compilador avisa si un {@code
 * switch} sobre eventos se deja alguno.
 */
public sealed interface DomainEvent permits OrderPlaced, OrderPaid, OrderShipped, OrderCancelled {

  OrderId orderId();

  Instant occurredAt();
}
