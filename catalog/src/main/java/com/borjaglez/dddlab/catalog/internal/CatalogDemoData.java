package com.borjaglez.dddlab.catalog.internal;

import java.math.BigDecimal;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/** Carga tres productos al arrancar para poder probar el API sin preparar nada. */
@Component
class CatalogDemoData implements ApplicationRunner {

  private final ProductRepository products;

  CatalogDemoData(ProductRepository products) {
    this.products = products;
  }

  @Override
  public void run(ApplicationArguments args) {
    if (products.count() > 0) {
      return;
    }
    products.save(product("keyboard", "Teclado mecánico", "Switches marrones, distribución ES", "89.90"));
    products.save(product("mouse", "Ratón inalámbrico", "Sensor de 16000 DPI", "29.95"));
    products.save(product("monitor", "Monitor 27 pulgadas", "QHD a 165 Hz", "349.00"));
  }

  private static Product product(String id, String name, String description, String price) {
    return new Product(id, name, description, new BigDecimal(price), "EUR");
  }
}
