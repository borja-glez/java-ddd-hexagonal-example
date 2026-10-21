package com.borjaglez.dddlab.catalog.internal;

import com.borjaglez.dddlab.catalog.api.ProductView;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/products")
class CatalogController {

  private final CatalogService catalog;

  CatalogController(CatalogService catalog) {
    this.catalog = catalog;
  }

  @GetMapping
  List<ProductView> list() {
    return catalog.listProducts();
  }

  @GetMapping("/{productId}")
  ResponseEntity<ProductView> get(@PathVariable String productId) {
    return ResponseEntity.of(catalog.findProduct(productId));
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  ProductView create(@RequestBody NewProductRequest request) {
    return catalog.create(request.id(), request.name(), request.description(), request.price());
  }

  @ExceptionHandler(IllegalArgumentException.class)
  ProblemDetail onInvalidProduct(IllegalArgumentException exception) {
    return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
  }

  record NewProductRequest(String id, String name, String description, BigDecimal price) {}
}
