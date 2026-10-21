package com.borjaglez.dddlab.ordering.infrastructure.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.math.BigDecimal;

/**
 * Una línea tal como se guarda. En el dominio {@code OrderLine} es una entidad; en la base de datos
 * basta una colección de valores dentro del pedido: el modelo de persistencia no tiene por qué
 * copiar el modelo de dominio.
 */
@Embeddable
class OrderLineEmbeddable {

  @Column(nullable = false)
  private String productId;

  @Column(nullable = false)
  private String productName;

  @Column(nullable = false)
  private BigDecimal unitPrice;

  @Column(nullable = false, length = 3)
  private String currency;

  @Column(nullable = false)
  private int quantity;

  protected OrderLineEmbeddable() {}

  OrderLineEmbeddable(
      String productId, String productName, BigDecimal unitPrice, String currency, int quantity) {
    this.productId = productId;
    this.productName = productName;
    this.unitPrice = unitPrice;
    this.currency = currency;
    this.quantity = quantity;
  }

  String getProductId() {
    return productId;
  }

  String getProductName() {
    return productName;
  }

  BigDecimal getUnitPrice() {
    return unitPrice;
  }

  String getCurrency() {
    return currency;
  }

  int getQuantity() {
    return quantity;
  }
}
