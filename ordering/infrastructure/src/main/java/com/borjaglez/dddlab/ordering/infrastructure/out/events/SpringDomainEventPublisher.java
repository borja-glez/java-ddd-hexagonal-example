package com.borjaglez.dddlab.ordering.infrastructure.out.events;

import com.borjaglez.dddlab.ordering.application.port.out.DomainEventPublisher;
import com.borjaglez.dddlab.ordering.domain.event.DomainEvent;
import java.util.List;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * [Hexagonal · adaptador de salida (driven)] Implementa {@link DomainEventPublisher} con los eventos
 * en memoria de Spring.
 *
 * <p>Los eventos se publican dentro de la transacción del caso de uso; quien los escucha con
 * {@code @TransactionalEventListener} los recibe solo si esa transacción hace commit.
 */
@Component
class SpringDomainEventPublisher implements DomainEventPublisher {

  private final ApplicationEventPublisher springEvents;

  SpringDomainEventPublisher(ApplicationEventPublisher springEvents) {
    this.springEvents = springEvents;
  }

  @Override
  public void publish(List<DomainEvent> events) {
    events.forEach(springEvents::publishEvent);
  }
}
