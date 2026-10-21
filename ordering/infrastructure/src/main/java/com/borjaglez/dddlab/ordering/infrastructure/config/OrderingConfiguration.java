package com.borjaglez.dddlab.ordering.infrastructure.config;

import com.borjaglez.dddlab.ordering.application.port.in.AddProductToOrderUseCase;
import com.borjaglez.dddlab.ordering.application.port.in.CancelExpiredOrdersUseCase;
import com.borjaglez.dddlab.ordering.application.port.in.CancelOrderUseCase;
import com.borjaglez.dddlab.ordering.application.port.in.GetOrderQuery;
import com.borjaglez.dddlab.ordering.application.port.in.PayOrderUseCase;
import com.borjaglez.dddlab.ordering.application.port.in.PlaceOrderUseCase;
import com.borjaglez.dddlab.ordering.application.port.in.RefundCancelledOrderUseCase;
import com.borjaglez.dddlab.ordering.application.port.in.ShipOrderUseCase;
import com.borjaglez.dddlab.ordering.application.port.out.DomainEventPublisher;
import com.borjaglez.dddlab.ordering.application.port.out.OrderViews;
import com.borjaglez.dddlab.ordering.application.port.out.PaymentGateway;
import com.borjaglez.dddlab.ordering.application.port.out.ProductCatalog;
import com.borjaglez.dddlab.ordering.application.service.AddProductToOrderService;
import com.borjaglez.dddlab.ordering.application.service.CancelExpiredOrdersService;
import com.borjaglez.dddlab.ordering.application.service.CancelOrderService;
import com.borjaglez.dddlab.ordering.application.service.GetOrderService;
import com.borjaglez.dddlab.ordering.application.service.PayOrderService;
import com.borjaglez.dddlab.ordering.application.service.PlaceOrderService;
import com.borjaglez.dddlab.ordering.application.service.RefundCancelledOrderService;
import com.borjaglez.dddlab.ordering.application.service.ShipOrderService;
import com.borjaglez.dddlab.ordering.domain.OrderRepository;
import com.borjaglez.dddlab.ordering.domain.UnpaidOrdersLimit;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * [Hexagonal · composition root de Ordering] Aquí, y solo aquí, Spring conoce el núcleo.
 *
 * <p>Las clases del dominio y de la aplicación no llevan anotaciones: se crean con {@code new} y se
 * registran como beans desde este anillo exterior. Los adaptadores sí son {@code @Component} porque
 * viven fuera del núcleo; Spring los inyecta donde el núcleo pide un puerto (inversión de
 * dependencias).
 *
 * <p>La transacción tampoco está en el núcleo: cada caso de uso que escribe se envuelve aquí en una
 * {@link TransactionTemplate}. Como los puertos de entrada tienen un único método, el envoltorio es
 * una lambda. Una transacción por caso de uso, y cada caso de uso modifica un solo agregado.
 */
@Configuration(proxyBeanMethods = false)
@EnableScheduling
class OrderingConfiguration {

  /** El tiempo también es una dependencia: los tests usan un reloj fijo. */
  @Bean
  Clock clock() {
    return Clock.systemUTC();
  }

  @Bean
  UnpaidOrdersLimit unpaidOrdersLimit(OrderRepository orders) {
    return new UnpaidOrdersLimit(orders);
  }

  @Bean
  PlaceOrderUseCase placeOrderUseCase(
      OrderRepository orders,
      ProductCatalog catalog,
      UnpaidOrdersLimit unpaidOrdersLimit,
      DomainEventPublisher events,
      Clock clock,
      PlatformTransactionManager transactionManager) {
    var useCase = new PlaceOrderService(orders, catalog, unpaidOrdersLimit, events, clock);
    var tx = new TransactionTemplate(transactionManager);
    return command -> tx.execute(status -> useCase.place(command));
  }

  @Bean
  AddProductToOrderUseCase addProductToOrderUseCase(
      OrderRepository orders,
      ProductCatalog catalog,
      PlatformTransactionManager transactionManager) {
    var useCase = new AddProductToOrderService(orders, catalog);
    var tx = new TransactionTemplate(transactionManager);
    return command -> tx.executeWithoutResult(status -> useCase.addProduct(command));
  }

  @Bean
  PayOrderUseCase payOrderUseCase(
      OrderRepository orders,
      PaymentGateway paymentGateway,
      DomainEventPublisher events,
      Clock clock,
      PlatformTransactionManager transactionManager) {
    var useCase = new PayOrderService(orders, paymentGateway, events, clock);
    var tx = new TransactionTemplate(transactionManager);
    return orderId -> tx.executeWithoutResult(status -> useCase.pay(orderId));
  }

  @Bean
  ShipOrderUseCase shipOrderUseCase(
      OrderRepository orders,
      DomainEventPublisher events,
      Clock clock,
      PlatformTransactionManager transactionManager) {
    var useCase = new ShipOrderService(orders, events, clock);
    var tx = new TransactionTemplate(transactionManager);
    return orderId -> tx.executeWithoutResult(status -> useCase.ship(orderId));
  }

  @Bean
  CancelOrderUseCase cancelOrderUseCase(
      OrderRepository orders,
      DomainEventPublisher events,
      Clock clock,
      PlatformTransactionManager transactionManager) {
    var useCase = new CancelOrderService(orders, events, clock);
    var tx = new TransactionTemplate(transactionManager);
    return command -> tx.executeWithoutResult(status -> useCase.cancel(command));
  }

  /** Sin transacción propia: cada pedido se cancela en la suya, a través de cancelOrderUseCase. */
  @Bean
  CancelExpiredOrdersUseCase cancelExpiredOrdersUseCase(
      OrderRepository orders, CancelOrderUseCase cancelOrderUseCase, Clock clock) {
    return new CancelExpiredOrdersService(orders, cancelOrderUseCase, clock);
  }

  /** Sin transacción: no toca la base de datos, solo llama a la pasarela. */
  @Bean
  RefundCancelledOrderUseCase refundCancelledOrderUseCase(PaymentGateway paymentGateway) {
    return new RefundCancelledOrderService(paymentGateway);
  }

  @Bean
  GetOrderQuery getOrderQuery(
      OrderViews orderViews, PlatformTransactionManager transactionManager) {
    var query = new GetOrderService(orderViews);
    var tx = new TransactionTemplate(transactionManager);
    tx.setReadOnly(true);
    return orderId -> tx.execute(status -> query.get(orderId));
  }
}
