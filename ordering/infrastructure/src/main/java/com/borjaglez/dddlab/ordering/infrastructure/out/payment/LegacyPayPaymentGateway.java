package com.borjaglez.dddlab.ordering.infrastructure.out.payment;

import com.borjaglez.dddlab.ordering.application.port.out.PaymentGateway;
import com.borjaglez.dddlab.ordering.application.port.out.PaymentResult;
import com.borjaglez.dddlab.ordering.domain.OrderId;
import com.borjaglez.dddlab.ordering.domain.PaymentId;
import com.borjaglez.dddlab.sharedkernel.Money;
import com.legacypay.sdk.LegacyPayClient;
import com.legacypay.sdk.LpTransactionRequest;
import com.legacypay.sdk.LpTransactionResponse;
import org.springframework.stereotype.Component;

/**
 * [Hexagonal · adaptador de salida (driven)] [Context map · Anticorruption Layer] Implementa el
 * puerto {@link PaymentGateway} sobre el SDK de LegacyPay.
 *
 * <p>Es la frontera con un modelo ajeno. Traduce en las dos direcciones para que nada de LegacyPay
 * contamine Ordering:
 *
 * <ul>
 *   <li>{@link Money} → céntimos y código ISO; {@link OrderId} → {@code merchantRef}.
 *   <li>{@code respCode} "00" / "51" / "05" → {@link PaymentResult} con un motivo legible.
 *   <li>{@code txnId} → {@link PaymentId}.
 * </ul>
 *
 * <p>Pagos es un subdominio genérico: no se modela el cobro, se compra (LegacyPay) y solo se modela
 * la traducción. Si mañana se cambia a otra pasarela, se escribe otra clase como esta.
 */
@Component
class LegacyPayPaymentGateway implements PaymentGateway {

  private static final String APPROVED = "00";
  private static final String INSUFFICIENT_FUNDS = "51";
  private static final String DO_NOT_HONOR = "05";

  private final LegacyPayClient client;

  LegacyPayPaymentGateway(LegacyPayClient client) {
    this.client = client;
  }

  @Override
  public PaymentResult charge(OrderId orderId, Money amount) {
    LpTransactionRequest request = new LpTransactionRequest();
    request.setTxType("AUTH");
    request.setMerchantRef(orderId.toString());
    request.setAmountInCents(toCents(amount));
    request.setCurrencyIso(amount.currency().getCurrencyCode());

    LpTransactionResponse response = client.doTransaction(request);

    return switch (response.getRespCode()) {
      case APPROVED -> new PaymentResult.Approved(new PaymentId(response.getTxnId()));
      case INSUFFICIENT_FUNDS -> new PaymentResult.Rejected("fondos insuficientes");
      case DO_NOT_HONOR -> new PaymentResult.Rejected("el banco ha denegado la operación");
      default -> new PaymentResult.Rejected("respuesta desconocida de la pasarela");
    };
  }

  @Override
  public void refund(PaymentId paymentId, Money amount) {
    LpTransactionRequest request = new LpTransactionRequest();
    request.setTxType("RFND");
    request.setOriginalTxnId(paymentId.value());
    request.setAmountInCents(toCents(amount));
    request.setCurrencyIso(amount.currency().getCurrencyCode());

    LpTransactionResponse response = client.doTransaction(request);

    if (!APPROVED.equals(response.getRespCode())) {
      throw new IllegalStateException(
          "LegacyPay ha rechazado el reembolso de " + paymentId + ": " + response.getRespCode());
    }
  }

  /** 89.90 EUR → 8990. Money ya garantiza dos decimales, así que la conversión es exacta. */
  private static long toCents(Money amount) {
    return amount.amount().movePointRight(2).longValueExact();
  }
}
