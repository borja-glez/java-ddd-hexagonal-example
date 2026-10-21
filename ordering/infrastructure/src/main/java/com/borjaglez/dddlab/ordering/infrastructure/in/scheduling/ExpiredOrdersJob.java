package com.borjaglez.dddlab.ordering.infrastructure.in.scheduling;

import com.borjaglez.dddlab.ordering.application.port.in.CancelExpiredOrdersUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * [Hexagonal · adaptador de entrada (driving)] Un job programado.
 *
 * <p>Otra forma de entrar en la aplicación, igual que el controlador REST. No sabe qué es un pedido
 * caducado (eso es {@code Order.PAYMENT_DEADLINE}) ni cómo se cancela (eso es {@code Order.cancel}):
 * solo decide cuándo se ejecuta.
 */
@Component
class ExpiredOrdersJob {

  private static final Logger log = LoggerFactory.getLogger(ExpiredOrdersJob.class);

  private final CancelExpiredOrdersUseCase cancelExpiredOrders;

  ExpiredOrdersJob(CancelExpiredOrdersUseCase cancelExpiredOrders) {
    this.cancelExpiredOrders = cancelExpiredOrders;
  }

  @Scheduled(
      initialDelayString = "${ordering.expired-orders.check-interval}",
      fixedDelayString = "${ordering.expired-orders.check-interval}")
  void run() {
    int cancelled = cancelExpiredOrders.cancelExpiredOrders();
    if (cancelled > 0) {
      log.info("Cancelados {} pedidos sin pagar fuera de plazo", cancelled);
    }
  }
}
