package com.borjaglez.dddlab.ordering.application.service;

import static java.util.Objects.requireNonNull;

import com.borjaglez.dddlab.ordering.application.exception.OrderNotFound;
import com.borjaglez.dddlab.ordering.application.exception.ProductNotFound;
import com.borjaglez.dddlab.ordering.application.port.in.AddProductToOrderCommand;
import com.borjaglez.dddlab.ordering.application.port.in.AddProductToOrderUseCase;
import com.borjaglez.dddlab.ordering.application.port.out.CatalogProduct;
import com.borjaglez.dddlab.ordering.application.port.out.ProductCatalog;
import com.borjaglez.dddlab.ordering.domain.Order;
import com.borjaglez.dddlab.ordering.domain.OrderLine;
import com.borjaglez.dddlab.ordering.domain.OrderRepository;

/** [DDD · Application Service] Caso de uso "añadir un producto a un pedido". */
public final class AddProductToOrderService implements AddProductToOrderUseCase {

  private final OrderRepository orders;
  private final ProductCatalog catalog;

  public AddProductToOrderService(OrderRepository orders, ProductCatalog catalog) {
    this.orders = requireNonNull(orders);
    this.catalog = requireNonNull(catalog);
  }

  @Override
  public void addProduct(AddProductToOrderCommand command) {
    Order order =
        orders.findById(command.orderId()).orElseThrow(() -> new OrderNotFound(command.orderId()));
    CatalogProduct product =
        catalog
            .findById(command.productId())
            .orElseThrow(() -> new ProductNotFound(command.productId()));

    order.addLine(new OrderLine(product.id(), product.name(), product.price(), command.quantity()));

    orders.save(order);
  }
}
