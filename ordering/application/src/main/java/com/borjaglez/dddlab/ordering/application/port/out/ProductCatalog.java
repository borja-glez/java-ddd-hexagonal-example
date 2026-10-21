package com.borjaglez.dddlab.ordering.application.port.out;

import com.borjaglez.dddlab.ordering.domain.ProductId;
import java.util.Optional;

/**
 * [Hexagonal · puerto de salida] Lo que Ordering necesita saber del catálogo: nombre y precio
 * actual de un producto.
 *
 * <p>Lo define Ordering, a su medida y en su idioma. Que detrás esté el módulo Catalog, un API HTTP o
 * un fichero es cosa del adaptador.
 */
public interface ProductCatalog {

  Optional<CatalogProduct> findById(ProductId productId);
}
