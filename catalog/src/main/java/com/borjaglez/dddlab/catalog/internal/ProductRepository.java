package com.borjaglez.dddlab.catalog.internal;

import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data directo, sin puerto delante: en un CRUD no hay núcleo que aislar. */
interface ProductRepository extends JpaRepository<Product, String> {}
