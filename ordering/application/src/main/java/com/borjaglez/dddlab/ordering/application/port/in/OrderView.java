package com.borjaglez.dddlab.ordering.application.port.in;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Modelo de lectura de un pedido: datos planos, sin comportamiento. No es el agregado ni la entidad
 * JPA, y puede cambiar de forma sin tocar ninguno de los dos.
 */
public record OrderView(
    String orderId,
    String customerId,
    String status,
    List<Line> lines,
    BigDecimal total,
    String currency,
    String paymentId,
    Instant placedAt) {

  public record Line(
      String productId, String productName, BigDecimal unitPrice, int quantity, BigDecimal subtotal) {}
}
