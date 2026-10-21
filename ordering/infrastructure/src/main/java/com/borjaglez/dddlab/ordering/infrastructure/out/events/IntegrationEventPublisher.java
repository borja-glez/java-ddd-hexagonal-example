package com.borjaglez.dddlab.ordering.infrastructure.out.events;

import com.borjaglez.dddlab.ordering.contracts.OrderCancelledV1;
import com.borjaglez.dddlab.ordering.contracts.OrderPaidV1;
import com.borjaglez.dddlab.ordering.contracts.OrderPlacedV1;
import com.borjaglez.dddlab.ordering.contracts.OrderShippedV1;
import com.borjaglez.dddlab.ordering.domain.event.DomainEvent;
import com.borjaglez.dddlab.ordering.domain.event.OrderCancelled;
import com.borjaglez.dddlab.ordering.domain.event.OrderPaid;
import com.borjaglez.dddlab.ordering.domain.event.OrderPlaced;
import com.borjaglez.dddlab.ordering.domain.event.OrderShipped;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * [Context map · Open Host Service + Published Language] Traduce cada evento de dominio (interno,
 * con tipos de Ordering) a su evento de integración (público, versionado, solo tipos del JDK) y lo
 * publica para los demás contextos.
 *
 * <p>Separar los dos tipos de evento permite cambiar el modelo interno de Ordering sin romper a
 * quien lo escucha. El {@code switch} sobre una interfaz {@code sealed} obliga a decidir qué se
 * publica cuando aparezca un evento de dominio nuevo: si falta un caso, no compila.
 *
 * <p>Producción: en vez de eventos de Spring en memoria, esto escribiría en una tabla outbox en la
 * misma transacción y un relay lo enviaría a Kafka. Cambiaría este adaptador, nada más.
 */
@Component
class IntegrationEventPublisher {

  private final ApplicationEventPublisher springEvents;

  IntegrationEventPublisher(ApplicationEventPublisher springEvents) {
    this.springEvents = springEvents;
  }

  @TransactionalEventListener
  void on(DomainEvent event) {
    springEvents.publishEvent(toPublishedLanguage(event));
  }

  static Object toPublishedLanguage(DomainEvent event) {
    return switch (event) {
      case OrderPlaced placed ->
          new OrderPlacedV1(
              placed.orderId().toString(),
              placed.customerId().value(),
              placed.total().amount(),
              placed.total().currency().getCurrencyCode(),
              placed.occurredAt());
      case OrderPaid paid ->
          new OrderPaidV1(
              paid.orderId().toString(),
              paid.customerId().value(),
              paid.amount().amount(),
              paid.amount().currency().getCurrencyCode(),
              paid.occurredAt());
      case OrderShipped shipped ->
          new OrderShippedV1(
              shipped.orderId().toString(), shipped.customerId().value(), shipped.occurredAt());
      case OrderCancelled cancelled ->
          new OrderCancelledV1(
              cancelled.orderId().toString(),
              cancelled.customerId().value(),
              cancelled.reason().name(),
              cancelled.wasPaid(),
              cancelled.occurredAt());
    };
  }
}
