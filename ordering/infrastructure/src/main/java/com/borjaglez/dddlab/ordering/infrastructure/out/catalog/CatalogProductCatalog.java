package com.borjaglez.dddlab.ordering.infrastructure.out.catalog;

import com.borjaglez.dddlab.catalog.api.CatalogApi;
import com.borjaglez.dddlab.catalog.api.ProductView;
import com.borjaglez.dddlab.ordering.application.port.out.CatalogProduct;
import com.borjaglez.dddlab.ordering.application.port.out.ProductCatalog;
import com.borjaglez.dddlab.ordering.domain.ProductId;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * [Hexagonal · adaptador de salida (driven)] [Context map · Customer/Supplier] Implementa el puerto
 * {@link ProductCatalog} llamando al Open Host Service de Catalog.
 *
 * <p>Es el único sitio de Ordering que conoce Catalog, y solo su paquete {@code api}. Traduce el
 * {@link ProductView} de Catalog (con descripción, precio de hoy) a lo que Ordering necesita. Hoy es
 * una llamada en memoria porque es un monolito modular; si Catalog pasa a ser un microservicio, este
 * adaptador pasa a ser un cliente HTTP y nada más cambia.
 */
@Component
class CatalogProductCatalog implements ProductCatalog {

  private final CatalogApi catalogApi;

  CatalogProductCatalog(CatalogApi catalogApi) {
    this.catalogApi = catalogApi;
  }

  @Override
  public Optional<CatalogProduct> findById(ProductId productId) {
    return catalogApi.findProduct(productId.value()).map(CatalogProductCatalog::toOrdering);
  }

  private static CatalogProduct toOrdering(ProductView product) {
    return new CatalogProduct(new ProductId(product.id()), product.name(), product.price());
  }
}
