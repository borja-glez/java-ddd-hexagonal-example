package com.borjaglez.dddlab.catalog.internal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/**
 * Un producto del catálogo. Es una entidad JPA anémica (getters y setters, sin comportamiento) y
 * aquí eso es lo correcto.
 *
 * <p>Catalog es un subdominio supporting sin invariantes que proteger: crear un producto es validar
 * un formulario. Un agregado con value objects, puertos y mappers sería ceremonia sin retorno. DDD
 * estratégico sí (límite claro, su propio lenguaje, su propio esquema {@code catalog}); DDD táctico
 * y hexagonal, no.
 */
@Entity
@Table(name = "products", schema = "catalog")
public class Product {

  @Id private String id;

  @Column(nullable = false)
  private String name;

  private String description;

  @Column(nullable = false)
  private BigDecimal priceAmount;

  @Column(nullable = false, length = 3)
  private String priceCurrency;

  protected Product() {}

  public Product(
      String id, String name, String description, BigDecimal priceAmount, String priceCurrency) {
    this.id = id;
    this.name = name;
    this.description = description;
    this.priceAmount = priceAmount;
    this.priceCurrency = priceCurrency;
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public BigDecimal getPriceAmount() {
    return priceAmount;
  }

  public void setPriceAmount(BigDecimal priceAmount) {
    this.priceAmount = priceAmount;
  }

  public String getPriceCurrency() {
    return priceCurrency;
  }

  public void setPriceCurrency(String priceCurrency) {
    this.priceCurrency = priceCurrency;
  }
}
