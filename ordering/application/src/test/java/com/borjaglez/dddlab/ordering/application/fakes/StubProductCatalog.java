package com.borjaglez.dddlab.ordering.application.fakes;

import com.borjaglez.dddlab.ordering.application.port.out.CatalogProduct;
import com.borjaglez.dddlab.ordering.application.port.out.ProductCatalog;
import com.borjaglez.dddlab.ordering.domain.ProductId;
import com.borjaglez.dddlab.sharedkernel.Money;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/** Catálogo de test: los productos que el test necesita, con el precio que el test decide. */
public final class StubProductCatalog implements ProductCatalog {

  private final Map<ProductId, CatalogProduct> products = new HashMap<>();

  public StubProductCatalog with(String id, String name, String euros) {
    ProductId productId = new ProductId(id);
    products.put(productId, new CatalogProduct(productId, name, Money.euros(euros)));
    return this;
  }

  @Override
  public Optional<CatalogProduct> findById(ProductId productId) {
    return Optional.ofNullable(products.get(productId));
  }
}
