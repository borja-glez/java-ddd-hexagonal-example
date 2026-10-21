package com.borjaglez.dddlab.ordering.infrastructure.out.persistence;

import com.borjaglez.dddlab.ordering.domain.CustomerId;
import com.borjaglez.dddlab.ordering.domain.Order;
import com.borjaglez.dddlab.ordering.domain.OrderId;
import com.borjaglez.dddlab.ordering.domain.OrderRepository;
import com.borjaglez.dddlab.ordering.domain.OrderStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/**
 * [Hexagonal · adaptador de salida (driven)] Implementa el puerto {@link OrderRepository} del
 * dominio con JPA.
 *
 * <p>Recibe y devuelve {@link Order}: ni la entidad JPA ni Spring Data salen de este paquete.
 */
@Repository
class JpaOrderRepository implements OrderRepository {

  private final SpringDataOrderRepository jpa;

  JpaOrderRepository(SpringDataOrderRepository jpa) {
    this.jpa = jpa;
  }

  @Override
  public Optional<Order> findById(OrderId id) {
    return jpa.findById(id.value()).map(OrderMapper::toDomain);
  }

  /**
   * Copia el pedido sobre la entidad que el contexto de persistencia ya tiene cargada (la que leyó
   * {@link #findById} en esta misma transacción), así {@code @Version} detecta escrituras
   * concurrentes.
   */
  @Override
  public void save(Order order) {
    OrderJpaEntity entity =
        jpa.findById(order.id().value()).orElseGet(() -> new OrderJpaEntity(order.id().value()));
    OrderMapper.copyToEntity(order, entity);
    jpa.save(entity);
  }

  @Override
  public long countUnpaidBy(CustomerId customerId) {
    return jpa.countByCustomerIdAndStatus(customerId.value(), OrderStatus.PLACED.name());
  }

  @Override
  public List<OrderId> findUnpaidPlacedBefore(Instant moment) {
    return jpa.findIdsByStatusPlacedBefore(OrderStatus.PLACED.name(), moment).stream()
        .map(OrderId::new)
        .toList();
  }
}
