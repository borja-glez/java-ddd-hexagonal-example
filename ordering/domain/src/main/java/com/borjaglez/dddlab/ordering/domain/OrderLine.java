package com.borjaglez.dddlab.ordering.domain;

import static java.util.Objects.requireNonNull;

import com.borjaglez.dddlab.sharedkernel.Money;

/**
 * [DDD · Entity interna del agregado] Una línea del pedido.
 *
 * <p>Es una entidad y no un value object porque tiene identidad dentro del pedido (el producto) y
 * cambia con el tiempo: si se añade otra vez el mismo producto, esta línea suma cantidad. Solo el
 * agregado la modifica: {@link #increaseBy} es package-private, así que fuera del paquete del
 * dominio nadie puede saltarse a {@link Order}.
 *
 * <p>[Bounded context] "Producto" en Ordering es esto: un id, un nombre y un precio <b>congelado en
 * el momento de la compra</b>. Si Catalog sube el precio mañana, este pedido no cambia.
 */
public final class OrderLine {

  private final ProductId productId;
  private final String productName;
  private final Money unitPrice;
  private Quantity quantity;

  public OrderLine(ProductId productId, String productName, Money unitPrice, Quantity quantity) {
    this.productId = requireNonNull(productId, "productId");
    this.unitPrice = requireNonNull(unitPrice, "unitPrice");
    this.quantity = requireNonNull(quantity, "quantity");
    if (productName == null || productName.isBlank()) {
      throw new IllegalArgumentException("Una línea necesita el nombre del producto");
    }
    this.productName = productName;
  }

  void increaseBy(Quantity extra) {
    this.quantity = quantity.add(extra);
  }

  public Money subtotal() {
    return unitPrice.multiply(quantity.value());
  }

  public ProductId productId() {
    return productId;
  }

  public String productName() {
    return productName;
  }

  public Money unitPrice() {
    return unitPrice;
  }

  public Quantity quantity() {
    return quantity;
  }

  /** Identidad local: dentro de un pedido, una línea por producto. */
  @Override
  public boolean equals(Object other) {
    return other instanceof OrderLine line && productId.equals(line.productId);
  }

  @Override
  public int hashCode() {
    return productId.hashCode();
  }
}
