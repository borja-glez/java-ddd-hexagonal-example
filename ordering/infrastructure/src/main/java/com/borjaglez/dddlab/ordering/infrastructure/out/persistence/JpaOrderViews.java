package com.borjaglez.dddlab.ordering.infrastructure.out.persistence;

import com.borjaglez.dddlab.ordering.application.port.in.OrderView;
import com.borjaglez.dddlab.ordering.application.port.out.OrderViews;
import com.borjaglez.dddlab.ordering.domain.OrderId;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * [Hexagonal · adaptador de salida · lectura] Construye la vista directamente desde las tablas, sin
 * reconstruir el agregado. Aquí podría haber una consulta SQL a medida o una tabla de proyección.
 */
@Component
class JpaOrderViews implements OrderViews {

  private final SpringDataOrderRepository jpa;

  JpaOrderViews(SpringDataOrderRepository jpa) {
    this.jpa = jpa;
  }

  @Override
  public Optional<OrderView> findById(OrderId orderId) {
    return jpa.findById(orderId.value()).map(OrderMapper::toView);
  }
}
