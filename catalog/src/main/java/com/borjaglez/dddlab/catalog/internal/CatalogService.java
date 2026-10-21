package com.borjaglez.dddlab.catalog.internal;

import com.borjaglez.dddlab.catalog.api.CatalogApi;
import com.borjaglez.dddlab.catalog.api.ProductView;
import com.borjaglez.dddlab.sharedkernel.Money;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Capas clásicas (nivel 0): Controller → Service → Repository, con {@code @Service} y {@code
 * @Transactional} sin complejos. Es un "transaction script": cada método hace su trabajo de arriba
 * abajo. Comparado con Ordering, sirve para ver cuánto cuesta cada pieza de DDD táctico y por qué
 * solo se paga donde hay reglas.
 */
@Service
@Transactional(readOnly = true)
class CatalogService implements CatalogApi {

  private final ProductRepository products;

  CatalogService(ProductRepository products) {
    this.products = products;
  }

  @Override
  public Optional<ProductView> findProduct(String productId) {
    return products.findById(productId).map(CatalogService::toView);
  }

  @Override
  public List<ProductView> listProducts() {
    return products.findAll().stream().map(CatalogService::toView).toList();
  }

  @Transactional
  public ProductView create(String id, String name, String description, BigDecimal price) {
    if (products.existsById(id)) {
      throw new IllegalArgumentException("Ya existe un producto con id " + id);
    }
    return toView(products.save(new Product(id, name, description, price, "EUR")));
  }

  private static ProductView toView(Product product) {
    Money price =
        new Money(product.getPriceAmount(), Currency.getInstance(product.getPriceCurrency()));
    return new ProductView(product.getId(), product.getName(), product.getDescription(), price);
  }
}
