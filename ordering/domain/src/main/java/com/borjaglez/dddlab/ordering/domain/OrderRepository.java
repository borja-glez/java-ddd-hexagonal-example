package com.borjaglez.dddlab.ordering.domain;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * [DDD · Repository] [Hexagonal · puerto de salida] Los pedidos, vistos como una colección.
 *
 * <ul>
 *   <li>Carga y guarda el agregado completo (pedido + líneas): hay uno por raíz, no uno por tabla.
 *   <li>La interfaz es del dominio y habla su idioma: recibe y devuelve {@link Order}, nunca
 *       entidades JPA ni {@code Page} de Spring.
 *   <li>La implementación (JPA) vive en infraestructura y el dominio no sabe que existe.
 * </ul>
 */
public interface OrderRepository {

  Optional<Order> findById(OrderId id);

  void save(Order order);

  /** Pedidos realizados y aún sin pagar de un cliente (lo usa {@link UnpaidOrdersLimit}). */
  long countUnpaidBy(CustomerId customerId);

  /** Pedidos sin pagar realizados antes de ese instante (candidatos a caducar). */
  List<OrderId> findUnpaidPlacedBefore(Instant moment);
}
