package com.borjaglez.dddlab.sharedkernel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

/** Un Value Object se prueba sin nada alrededor: ni Spring, ni base de datos, ni mocks. */
class MoneyTest {

  @Test
  void twoAmountsWithTheSameValueAreEqual() {
    assertThat(Money.euros("10")).isEqualTo(Money.euros("10.00"));
  }

  @Test
  void anInvalidAmountCannotExist() {
    assertThatThrownBy(() -> Money.euros("-1"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("negativo");
  }

  @Test
  void operationsReturnANewValueAndLeaveTheOriginalUntouched() {
    Money price = Money.euros("29.95");

    Money threeUnits = price.multiply(3);

    assertThat(threeUnits).isEqualTo(Money.euros("89.85"));
    assertThat(price).isEqualTo(Money.euros("29.95"));
  }

  @Test
  void addsAmountsOfTheSameCurrency() {
    assertThat(Money.euros("10.50").add(Money.euros("0.25"))).isEqualTo(Money.euros("10.75"));
  }

  @Test
  void refusesToMixCurrencies() {
    assertThatThrownBy(() -> Money.euros("1").add(Money.of("1", "USD")))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("divisas distintas");
  }

  @Test
  void comparesAmounts() {
    assertThat(Money.euros("1000.01").isGreaterThan(Money.euros("1000"))).isTrue();
    assertThat(Money.euros("1000").isGreaterThan(Money.euros("1000"))).isFalse();
  }
}
