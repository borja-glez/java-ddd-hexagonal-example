package com.borjaglez.dddlab.ordering.application.port.out;

import com.borjaglez.dddlab.ordering.domain.ProductId;
import com.borjaglez.dddlab.sharedkernel.Money;

/** Un producto tal como lo ve Ordering: solo lo que necesita para congelar el precio en la línea. */
public record CatalogProduct(ProductId id, String name, Money price) {}
