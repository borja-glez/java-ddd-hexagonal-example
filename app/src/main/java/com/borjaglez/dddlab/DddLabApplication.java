package com.borjaglez.dddlab;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Un único desplegable con tres bounded contexts (Catalog, Ordering y Notifications): un monolito
 * modular. Cada contexto vive en su módulo y su esquema de base de datos, y se comunican solo por
 * su API pública o por eventos, así que extraer uno a un microservicio es mover un módulo, no
 * desenredarlo.
 */
@SpringBootApplication
public class DddLabApplication {

  public static void main(String[] args) {
    SpringApplication.run(DddLabApplication.class, args);
  }
}
