package com.borjaglez.dddlab.ordering.application.fakes;

import com.borjaglez.dddlab.ordering.domain.CustomerId;
import com.borjaglez.dddlab.ordering.domain.Order;
import com.borjaglez.dddlab.ordering.domain.OrderId;
import com.borjaglez.dddlab.ordering.domain.OrderLine;
import com.borjaglez.dddlab.ordering.domain.OrderRepository;
import com.borjaglez.dddlab.ordering.domain.OrderStatus;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * [Hexagonal · adaptador de salida de test] La segunda implementación del puerto {@link
 * OrderRepository}: la real es JPA, esta es un mapa.
 *
 * <p>Guarda y devuelve copias, como haría una base de datos: si un caso de uso cambia un pedido y no
 * llama a {@code save}, el cambio no queda guardado y el test lo detecta.
 */
public final class InMemoryOrderRepository implements OrderRepository {

  private final Map<OrderId, Order> orders = new LinkedHashMap<>();

  @Override
  public Optional<Order> findById(OrderId id) {
    return Optional.ofNullable(orders.get(id)).map(InMemoryOrderRepository::copy);
  }

  @Override
  public void save(Order order) {
    orders.put(order.id(), copy(order));
  }

  @Override
  public long countUnpaidBy(CustomerId customerId) {
    return orders.values().stream()
        .filter(order -> order.customerId().equals(customerId))
        .filter(order -> order.status() == OrderStatus.PLACED)
        .count();
  }

  @Override
  public List<OrderId> findUnpaidPlacedBefore(Instant moment) {
    return orders.values().stream()
        .filter(order -> order.status() == OrderStatus.PLACED)
        .filter(order -> order.placedAt().isBefore(moment))
        .map(Order::id)
        .toList();
  }

  private static Order copy(Order order) {
    List<OrderLine> lines =
        order.lines().stream()
            .map(l -> new OrderLine(l.productId(), l.productName(), l.unitPrice(), l.quantity()))
            .toList();
    return Order.reconstitute(
        order.id(),
        order.customerId(),
        lines,
        order.placedAt(),
        order.status(),
        order.paymentId().orElse(null));
  }
}
