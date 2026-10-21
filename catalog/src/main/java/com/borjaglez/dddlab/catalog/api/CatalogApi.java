package com.borjaglez.dddlab.catalog.api;

import java.util.List;
import java.util.Optional;

/**
 * [Context map · Open Host Service] La puerta pública del catálogo para otros contextos.
 *
 * <p>Catalog es upstream de Ordering (relación Customer/Supplier): ofrece justo lo que Ordering
 * necesita (consultar productos y su precio) y se compromete a mantenerlo estable. Todo lo que hay en
 * {@code catalog.internal} es privado y puede cambiar sin avisar a nadie: ArchitectureTest impide que
 * otro contexto lo use.
 */
public interface CatalogApi {

  Optional<ProductView> findProduct(String productId);

  List<ProductView> listProducts();
}
