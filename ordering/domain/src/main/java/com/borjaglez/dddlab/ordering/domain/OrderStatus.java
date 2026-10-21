package com.borjaglez.dddlab.ordering.domain;

/**
 * Ciclo de vida del pedido. Las transiciones son reglas de negocio, así que viven en el dominio.
 *
 * <pre>
 *   PLACED ──pagar──▶ PAID ──enviar──▶ SHIPPED
 *     │                 │
 *     └────cancelar─────┴────────────▶ CANCELLED
 * </pre>
 */
public enum OrderStatus {
  PLACED,
  PAID,
  SHIPPED,
  CANCELLED;

  boolean canTransitionTo(OrderStatus next) {
    return switch (this) {
      case PLACED -> next == PAID || next == CANCELLED;
      case PAID -> next == SHIPPED || next == CANCELLED;
      case SHIPPED, CANCELLED -> false;
    };
  }
}
