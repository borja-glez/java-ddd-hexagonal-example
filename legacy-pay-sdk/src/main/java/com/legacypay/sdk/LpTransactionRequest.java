package com.legacypay.sdk;

/** Petición de LegacyPay: un JavaBean mutable con los nombres y tipos del proveedor. */
public class LpTransactionRequest {

  /** "AUTH" para cobrar, "RFND" para devolver. */
  private String txType;

  /** Referencia del comercio (nuestro pedido). */
  private String merchantRef;

  /** Para "RFND": la operación original que se devuelve. */
  private String originalTxnId;

  private long amountInCents;
  private String currencyIso;

  public String getTxType() {
    return txType;
  }

  public void setTxType(String txType) {
    this.txType = txType;
  }

  public String getMerchantRef() {
    return merchantRef;
  }

  public void setMerchantRef(String merchantRef) {
    this.merchantRef = merchantRef;
  }

  public String getOriginalTxnId() {
    return originalTxnId;
  }

  public void setOriginalTxnId(String originalTxnId) {
    this.originalTxnId = originalTxnId;
  }

  public long getAmountInCents() {
    return amountInCents;
  }

  public void setAmountInCents(long amountInCents) {
    this.amountInCents = amountInCents;
  }

  public String getCurrencyIso() {
    return currencyIso;
  }

  public void setCurrencyIso(String currencyIso) {
    this.currencyIso = currencyIso;
  }
}
