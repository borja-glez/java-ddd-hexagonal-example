package com.borjaglez.dddlab.ordering.domain;

import com.borjaglez.dddlab.ordering.domain.event.DomainEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * [DDD · Aggregate Root] Base de las raíces de agregado.
 *
 * <p>Cuando el agregado cambia, anota el hecho como evento de dominio. No lo publica: no conoce
 * ningún bus ni ningún framework. El caso de uso recoge los eventos con {@link #pullDomainEvents()}
 * después de guardar el agregado y los entrega al puerto {@code DomainEventPublisher}.
 */
public abstract class AggregateRoot {

  private final List<DomainEvent> domainEvents = new ArrayList<>();

  protected void recordEvent(DomainEvent event) {
    domainEvents.add(event);
  }

  /** Devuelve los eventos anotados y vacía la lista, para que cada evento se publique una vez. */
  public List<DomainEvent> pullDomainEvents() {
    List<DomainEvent> events = List.copyOf(domainEvents);
    domainEvents.clear();
    return events;
  }
}
