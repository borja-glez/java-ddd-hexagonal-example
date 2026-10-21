package com.borjaglez.dddlab.ordering.infrastructure.in.rest;

import com.borjaglez.dddlab.ordering.application.port.in.AddProductToOrderCommand;
import com.borjaglez.dddlab.ordering.application.port.in.AddProductToOrderUseCase;
import com.borjaglez.dddlab.ordering.application.port.in.CancelOrderCommand;
import com.borjaglez.dddlab.ordering.application.port.in.CancelOrderUseCase;
import com.borjaglez.dddlab.ordering.application.port.in.GetOrderQuery;
import com.borjaglez.dddlab.ordering.application.port.in.OrderView;
import com.borjaglez.dddlab.ordering.application.port.in.PayOrderUseCase;
import com.borjaglez.dddlab.ordering.application.port.in.PlaceOrderCommand;
import com.borjaglez.dddlab.ordering.application.port.in.PlaceOrderUseCase;
import com.borjaglez.dddlab.ordering.application.port.in.ShipOrderUseCase;
import com.borjaglez.dddlab.ordering.domain.CancellationReason;
import com.borjaglez.dddlab.ordering.domain.CustomerId;
import com.borjaglez.dddlab.ordering.domain.OrderId;
import com.borjaglez.dddlab.ordering.domain.ProductId;
import com.borjaglez.dddlab.ordering.domain.Quantity;
import java.net.URI;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * [Hexagonal · adaptador de entrada (driving)] El API REST de pedidos.
 *
 * <p>Su único trabajo es traducir: JSON y rutas HTTP a comandos con value objects, y el resultado
 * del caso de uso a una respuesta HTTP. No decide nada de negocio: ni si un pedido se puede
 * cancelar, ni cuánto cuesta. Si mañana entra otro canal (gRPC, una cola), se escribe otro
 * adaptador que llama a los mismos puertos.
 */
@RestController
@RequestMapping("/orders")
class OrderController {

  private final PlaceOrderUseCase placeOrder;
  private final AddProductToOrderUseCase addProductToOrder;
  private final PayOrderUseCase payOrder;
  private final ShipOrderUseCase shipOrder;
  private final CancelOrderUseCase cancelOrder;
  private final GetOrderQuery getOrder;

  OrderController(
      PlaceOrderUseCase placeOrder,
      AddProductToOrderUseCase addProductToOrder,
      PayOrderUseCase payOrder,
      ShipOrderUseCase shipOrder,
      CancelOrderUseCase cancelOrder,
      GetOrderQuery getOrder) {
    this.placeOrder = placeOrder;
    this.addProductToOrder = addProductToOrder;
    this.payOrder = payOrder;
    this.shipOrder = shipOrder;
    this.cancelOrder = cancelOrder;
    this.getOrder = getOrder;
  }

  @PostMapping
  ResponseEntity<PlacedOrderResponse> place(@RequestBody PlaceOrderRequest request) {
    OrderId orderId = placeOrder.place(request.toCommand());
    return ResponseEntity.created(URI.create("/orders/" + orderId))
        .body(new PlacedOrderResponse(orderId.toString()));
  }

  @PostMapping("/{orderId}/lines")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  void addProduct(@PathVariable String orderId, @RequestBody OrderItemRequest request) {
    addProductToOrder.addProduct(
        new AddProductToOrderCommand(
            OrderId.of(orderId), new ProductId(request.productId()), new Quantity(request.quantity())));
  }

  @PostMapping("/{orderId}/payment")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  void pay(@PathVariable String orderId) {
    payOrder.pay(OrderId.of(orderId));
  }

  @PostMapping("/{orderId}/shipment")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  void ship(@PathVariable String orderId) {
    shipOrder.ship(OrderId.of(orderId));
  }

  @PostMapping("/{orderId}/cancellation")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  void cancel(@PathVariable String orderId) {
    cancelOrder.cancel(
        new CancelOrderCommand(OrderId.of(orderId), CancellationReason.CUSTOMER_REQUEST));
  }

  /** La vista de lectura ya tiene la forma que necesita el cliente: se devuelve tal cual. */
  @GetMapping("/{orderId}")
  OrderView get(@PathVariable String orderId) {
    return getOrder.get(OrderId.of(orderId));
  }

  record PlaceOrderRequest(String customerId, List<OrderItemRequest> items) {

    PlaceOrderCommand toCommand() {
      List<PlaceOrderCommand.Item> commandItems =
          items == null
              ? List.of()
              : items.stream()
                  .map(
                      item ->
                          new PlaceOrderCommand.Item(
                              new ProductId(item.productId()), new Quantity(item.quantity())))
                  .toList();
      return new PlaceOrderCommand(new CustomerId(customerId), commandItems);
    }
  }

  record OrderItemRequest(String productId, int quantity) {}

  record PlacedOrderResponse(String orderId) {}
}
