package com.borjaglez.dddlab.ordering.application.port.out;

import com.borjaglez.dddlab.ordering.domain.event.DomainEvent;
import java.util.List;

/**
 * [Hexagonal · puerto de salida] Publicar los eventos de dominio que ha anotado un agregado.
 *
 * <p>Hoy lo implementa un adaptador sobre los eventos de Spring (en memoria). Pasar a Kafka o a un
 * outbox transaccional es cambiar ese adaptador, no los casos de uso.
 */
public interface DomainEventPublisher {

  void publish(List<DomainEvent> events);
}
