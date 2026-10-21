package com.borjaglez.dddlab.ordering.application.exception;

import com.borjaglez.dddlab.ordering.domain.ProductId;

public final class ProductNotFound extends NotFoundException {

  public ProductNotFound(ProductId productId) {
    super("El catálogo no tiene el producto " + productId);
  }
}
