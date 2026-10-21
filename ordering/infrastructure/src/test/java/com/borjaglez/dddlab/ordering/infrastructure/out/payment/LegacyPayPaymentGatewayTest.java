package com.borjaglez.dddlab.ordering.infrastructure.out.payment;

import static org.assertj.core.api.Assertions.assertThat;

import com.borjaglez.dddlab.ordering.application.port.out.PaymentResult;
import com.borjaglez.dddlab.ordering.domain.OrderId;
import com.borjaglez.dddlab.ordering.domain.PaymentId;
import com.borjaglez.dddlab.sharedkernel.Money;
import com.legacypay.sdk.LegacyPayClient;
import com.legacypay.sdk.LpTransactionRequest;
import org.junit.jupiter.api.Test;

/** La ACL se prueba sola: lo que entra en términos de Ordering sale en términos de LegacyPay. */
class LegacyPayPaymentGatewayTest {

  private final LegacyPayClient legacyPay = new LegacyPayClient();
  private final LegacyPayPaymentGateway gateway = new LegacyPayPaymentGateway(legacyPay);

  @Test
  void translatesAChargeIntoLegacyPayTermsAndTheApprovalBack() {
    OrderId orderId = OrderId.newId();

    PaymentResult result = gateway.charge(orderId, Money.euros("89.90"));

    assertThat(result).isInstanceOf(PaymentResult.Approved.class);
    LpTransactionRequest sent = legacyPay.getSandboxLedger().getFirst();
    assertThat(sent.getTxType()).isEqualTo("AUTH");
    assertThat(sent.getMerchantRef()).isEqualTo(orderId.toString());
    assertThat(sent.getAmountInCents()).isEqualTo(8990L);
    assertThat(sent.getCurrencyIso()).isEqualTo("EUR");
  }

  @Test
  void translatesCrypticRejectionCodesIntoReadableReasons() {
    assertThat(gateway.charge(OrderId.newId(), Money.euros("1000.01")))
        .isEqualTo(new PaymentResult.Rejected("fondos insuficientes"));
    assertThat(gateway.charge(OrderId.newId(), Money.of("10", "USD")))
        .isEqualTo(new PaymentResult.Rejected("el banco ha denegado la operación"));
  }

  @Test
  void refundsTheOriginalTransaction() {
    gateway.refund(new PaymentId("LP-1001"), Money.euros("29.95"));

    LpTransactionRequest sent = legacyPay.getSandboxLedger().getFirst();
    assertThat(sent.getTxType()).isEqualTo("RFND");
    assertThat(sent.getOriginalTxnId()).isEqualTo("LP-1001");
    assertThat(sent.getAmountInCents()).isEqualTo(2995L);
  }
}
