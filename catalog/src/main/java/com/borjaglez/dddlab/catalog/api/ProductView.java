package com.borjaglez.dddlab.catalog.api;

import com.borjaglez.dddlab.sharedkernel.Money;

/**
 * [Bounded context] "Producto" en Catalog: lo que se enseña en la tienda (nombre, descripción y
 * precio de hoy). En Ordering, en cambio, un producto es una línea con el precio congelado.
 *
 * <p>Es el lenguaje que publica el Open Host Service. Usa {@link Money} del Shared Kernel.
 */
public record ProductView(String id, String name, String description, Money price) {}
