package com.legacypay.sdk;

/**
 * Respuesta de LegacyPay.
 *
 * <p>{@code respCode}: "00" aprobada, "51" fondos insuficientes, "05" denegada por el banco.
 */
public class LpTransactionResponse {

  private String respCode;
  private String txnId;

  public String getRespCode() {
    return respCode;
  }

  public void setRespCode(String respCode) {
    this.respCode = respCode;
  }

  public String getTxnId() {
    return txnId;
  }

  public void setTxnId(String txnId) {
    this.txnId = txnId;
  }
}
