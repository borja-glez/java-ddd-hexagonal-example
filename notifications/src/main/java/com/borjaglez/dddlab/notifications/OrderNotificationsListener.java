package com.borjaglez.dddlab.notifications;

import com.borjaglez.dddlab.ordering.contracts.OrderCancelledV1;
import com.borjaglez.dddlab.ordering.contracts.OrderPaidV1;
import com.borjaglez.dddlab.ordering.contracts.OrderShippedV1;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * [Context map · Conformist] Notifications usa los eventos de integración de Ordering tal cual, sin
 * traducirlos a un modelo propio: para mandar un email no merece la pena.
 *
 * <p>Solo depende del módulo {@code ordering:contracts}. No ve {@code Order}, ni los eventos de
 * dominio, ni la base de datos de Ordering. Añadir este contexto no tocó ni una línea de Ordering:
 * es un suscriptor más.
 */
@Component
class OrderNotificationsListener {

  private final EmailSender email;

  OrderNotificationsListener(EmailSender email) {
    this.email = email;
  }

  @EventListener
  void on(OrderPaidV1 event) {
    String amount = event.amount() + " " + event.currency();
    email.send(
        event.customerId(),
        "Pago recibido",
        "Hemos cobrado " + amount + " del pedido " + event.orderId());
  }

  @EventListener
  void on(OrderShippedV1 event) {
    email.send(
        event.customerId(), "Pedido enviado", "El pedido " + event.orderId() + " va en camino");
  }

  @EventListener
  void on(OrderCancelledV1 event) {
    String refund = event.refundIssued() ? " Te devolvemos el importe." : "";
    email.send(
        event.customerId(),
        "Pedido cancelado",
        "El pedido " + event.orderId() + " se ha cancelado." + refund);
  }
}
