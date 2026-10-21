package com.borjaglez.dddlab.ordering.application.fakes;

import com.borjaglez.dddlab.ordering.application.port.out.PaymentGateway;
import com.borjaglez.dddlab.ordering.application.port.out.PaymentResult;
import com.borjaglez.dddlab.ordering.domain.OrderId;
import com.borjaglez.dddlab.ordering.domain.PaymentId;
import com.borjaglez.dddlab.sharedkernel.Money;
import java.util.ArrayList;
import java.util.List;

/**
 * Pasarela de test: aprueba todo salvo que se le pida rechazar, y apunta lo que se le pide. Con un
 * puerto de salida, probar el camino "la pasarela rechaza" no necesita levantar nada.
 */
public final class FakePaymentGateway implements PaymentGateway {

  public record Charge(OrderId orderId, Money amount) {}

  public record Refund(PaymentId paymentId, Money amount) {}

  private final List<Charge> charges = new ArrayList<>();
  private final List<Refund> refunds = new ArrayList<>();
  private String rejectionReason;

  public void rejectEverythingBecause(String reason) {
    this.rejectionReason = reason;
  }

  @Override
  public PaymentResult charge(OrderId orderId, Money amount) {
    charges.add(new Charge(orderId, amount));
    if (rejectionReason != null) {
      return new PaymentResult.Rejected(rejectionReason);
    }
    return new PaymentResult.Approved(new PaymentId("PAY-" + charges.size()));
  }

  @Override
  public void refund(PaymentId paymentId, Money amount) {
    refunds.add(new Refund(paymentId, amount));
  }

  public List<Charge> charges() {
    return List.copyOf(charges);
  }

  public List<Refund> refunds() {
    return List.copyOf(refunds);
  }
}
