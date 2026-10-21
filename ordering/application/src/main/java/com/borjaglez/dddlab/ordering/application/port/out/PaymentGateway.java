package com.borjaglez.dddlab.ordering.application.port.out;

import com.borjaglez.dddlab.ordering.domain.OrderId;
import com.borjaglez.dddlab.ordering.domain.PaymentId;
import com.borjaglez.dddlab.sharedkernel.Money;

/**
 * [Hexagonal · puerto de salida] Cobrar y devolver dinero.
 *
 * <p>Habla el lenguaje del negocio ({@code charge}, {@code refund}, {@link Money}), no el de la
 * pasarela ({@code doTransaction}, céntimos, códigos "00"/"51"). Cambiar de pasarela es escribir
 * otro adaptador de este puerto; el dominio y los casos de uso no se enteran.
 */
public interface PaymentGateway {

  PaymentResult charge(OrderId orderId, Money amount);

  void refund(PaymentId paymentId, Money amount);
}
