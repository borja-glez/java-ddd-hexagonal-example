package com.borjaglez.dddlab.ordering.application.fakes;

import com.borjaglez.dddlab.ordering.application.port.out.DomainEventPublisher;
import com.borjaglez.dddlab.ordering.domain.event.DomainEvent;
import java.util.ArrayList;
import java.util.List;

/** Publicador de test: en vez de enviar los eventos, los guarda para que el test los mire. */
public final class RecordingEventPublisher implements DomainEventPublisher {

  private final List<DomainEvent> published = new ArrayList<>();

  @Override
  public void publish(List<DomainEvent> events) {
    published.addAll(events);
  }

  public List<DomainEvent> published() {
    return List.copyOf(published);
  }
}
