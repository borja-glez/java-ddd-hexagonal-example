package com.legacypay.sdk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Cliente de "LegacyPay", una pasarela de pagos ficticia con un API de los de antes: JavaBeans
 * mutables, importes en céntimos, códigos de respuesta crípticos y tipos de operación en strings.
 *
 * <p>Este módulo NO es nuestro código: representa una librería de terceros que no podemos cambiar.
 * Funciona en modo sandbox con reglas fijas para poder probar los dos caminos:
 *
 * <ul>
 *   <li>{@code AUTH} de más de 1000,00 (100000 céntimos) responde {@code 51} (fondos insuficientes).
 *   <li>{@code AUTH} en una divisa distinta de EUR responde {@code 05} (operación denegada).
 *   <li>El resto responde {@code 00} (aprobada) con un {@code txnId} nuevo.
 * </ul>
 */
public class LegacyPayClient {

  private final AtomicInteger sequence = new AtomicInteger(1000);
  private final List<LpTransactionRequest> sandboxLedger =
      Collections.synchronizedList(new ArrayList<>());

  public LpTransactionResponse doTransaction(LpTransactionRequest request) {
    sandboxLedger.add(request);
    LpTransactionResponse response = new LpTransactionResponse();
    if ("AUTH".equals(request.getTxType()) && !"EUR".equals(request.getCurrencyIso())) {
      response.setRespCode("05");
    } else if ("AUTH".equals(request.getTxType()) && request.getAmountInCents() > 100_000) {
      response.setRespCode("51");
    } else {
      response.setRespCode("00");
      response.setTxnId("LP-" + sequence.incrementAndGet());
    }
    return response;
  }

  /** Todas las operaciones recibidas en modo sandbox, en orden. */
  public List<LpTransactionRequest> getSandboxLedger() {
    synchronized (sandboxLedger) {
      return List.copyOf(sandboxLedger);
    }
  }
}
