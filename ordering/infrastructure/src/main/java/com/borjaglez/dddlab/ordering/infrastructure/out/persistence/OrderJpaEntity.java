package com.borjaglez.dddlab.ordering.infrastructure.out.persistence;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Modelo de persistencia del pedido: cómo se guarda, no cómo se comporta.
 *
 * <p>Es una clase distinta de {@code Order} a propósito (opción "purista"): las restricciones de JPA
 * (constructor vacío, setters, proxies, {@code @Version}) se quedan aquí y no deforman el dominio.
 * El precio de esa separación es un mapper ({@link OrderMapper}). Es package-private: fuera de este
 * adaptador nadie sabe que existe.
 */
@Entity
@Table(name = "orders", schema = "ordering")
class OrderJpaEntity {

  @Id private UUID id;

  @Column(nullable = false)
  private String customerId;

  @Column(nullable = false)
  private String status;

  private String paymentId;

  @Column(nullable = false)
  private Instant placedAt;

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(
      name = "order_lines",
      schema = "ordering",
      joinColumns = @JoinColumn(name = "order_id"))
  @OrderColumn(name = "line_position")
  private List<OrderLineEmbeddable> lines = new ArrayList<>();

  /**
   * Bloqueo optimista. El dominio no sabe nada de versiones: si dos transacciones cambian el mismo
   * pedido a la vez, el UPDATE de la segunda no encuentra la versión que leyó y falla.
   */
  @Version private Long version;

  protected OrderJpaEntity() {}

  OrderJpaEntity(UUID id) {
    this.id = id;
  }

  UUID getId() {
    return id;
  }

  String getCustomerId() {
    return customerId;
  }

  void setCustomerId(String customerId) {
    this.customerId = customerId;
  }

  String getStatus() {
    return status;
  }

  void setStatus(String status) {
    this.status = status;
  }

  String getPaymentId() {
    return paymentId;
  }

  void setPaymentId(String paymentId) {
    this.paymentId = paymentId;
  }

  Instant getPlacedAt() {
    return placedAt;
  }

  void setPlacedAt(Instant placedAt) {
    this.placedAt = placedAt;
  }

  List<OrderLineEmbeddable> getLines() {
    return lines;
  }
}
