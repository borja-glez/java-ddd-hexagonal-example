package com.borjaglez.dddlab.ordering.infrastructure.out.persistence;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 * El repositorio de Spring Data. Es un detalle del adaptador, no el puerto: habla de entidades JPA
 * y estados en texto. El puerto {@code OrderRepository} lo envuelve y habla de {@code Order}.
 */
interface SpringDataOrderRepository extends JpaRepository<OrderJpaEntity, UUID> {

  long countByCustomerIdAndStatus(String customerId, String status);

  @Query("select o.id from OrderJpaEntity o where o.status = :status and o.placedAt < :moment")
  List<UUID> findIdsByStatusPlacedBefore(String status, Instant moment);
}
