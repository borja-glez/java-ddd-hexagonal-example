package com.borjaglez.dddlab.sharedkernel;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Objects;

/**
 * [DDD · Value Object] [Context map · Shared Kernel] Un importe en una divisa.
 *
 * <ul>
 *   <li>Se define por sus valores, no tiene identidad: {@code Money.euros("10")} es igual a
 *       cualquier otro {@code Money.euros("10.00")}.
 *   <li>Es inmutable: {@link #add} y {@link #multiply} devuelven un Money nuevo.
 *   <li>Se valida al construirse, así que un Money inválido (negativo, sin divisa) no puede existir.
 *       Sustituye a un {@code double} suelto: nada de redondeos raros ni importes sin divisa.
 * </ul>
 *
 * <p>Es el Shared Kernel entre Catalog y Ordering: los dos contextos usan esta misma clase, así que
 * cualquier cambio aquí se coordina entre ambos equipos. Por eso el kernel es diminuto.
 */
public record Money(BigDecimal amount, Currency currency) {

  public Money {
    Objects.requireNonNull(amount, "amount");
    Objects.requireNonNull(currency, "currency");
    if (amount.signum() < 0) {
      throw new IllegalArgumentException("Un importe no puede ser negativo: " + amount);
    }
    // Escala fija (2 decimales en EUR) para que la igualdad por valor funcione: 10 == 10.00.
    amount = amount.setScale(Math.max(currency.getDefaultFractionDigits(), 0), RoundingMode.HALF_EVEN);
  }

  public static Money of(String amount, String currencyCode) {
    return new Money(new BigDecimal(amount), Currency.getInstance(currencyCode));
  }

  public static Money euros(String amount) {
    return of(amount, "EUR");
  }

  public Money add(Money other) {
    requireSameCurrency(other);
    return new Money(amount.add(other.amount), currency);
  }

  public Money multiply(int factor) {
    if (factor < 0) {
      throw new IllegalArgumentException("No se puede multiplicar un importe por un número negativo");
    }
    return new Money(amount.multiply(BigDecimal.valueOf(factor)), currency);
  }

  public boolean isGreaterThan(Money other) {
    requireSameCurrency(other);
    return amount.compareTo(other.amount) > 0;
  }

  private void requireSameCurrency(Money other) {
    if (!currency.equals(other.currency)) {
      throw new IllegalArgumentException(
          "No se pueden operar importes en divisas distintas: " + this + " y " + other);
    }
  }

  @Override
  public String toString() {
    return amount.toPlainString() + " " + currency.getCurrencyCode();
  }
}
