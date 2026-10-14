package dev.jsamuelap.oikonomiaapi.shared.domain.validation;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import dev.jsamuelap.oikonomiaapi.shared.domain.exception.DomainException;

@DisplayName("Money rules validation")
public class MoneyRulesTest {
  @ParameterizedTest
  @DisplayName("Should accept valid amounts")
  @ValueSource(strings = {"0.01", "1", "10.5", "10.500", "9999999999.99", "1E+3"})
  void shouldAcceptValidAmounts(String value) {
    assertThatCode(() -> MoneyRules.requireValidAmount(new BigDecimal(value), "El monto")).doesNotThrowAnyException();
  }

  @Test
  @DisplayName("Should reject null amount")
  void shouldRejectNull() {
    assertThatThrownBy(() -> MoneyRules.requireValidAmount(null, "El monto")).isInstanceOf(DomainException.class)
      .hasMessageContaining("requerido");
  }

  @ParameterizedTest
  @DisplayName("Should reject non-positive amounts")
  @ValueSource(strings = {"0", "0.00", "-0.01", "-200"})
  void shouldRejectNonPositive(String value) {
    assertThatThrownBy(() -> MoneyRules.requireValidAmount(new BigDecimal(value), "El monto"))
      .isInstanceOf(DomainException.class).hasMessageContaining("mayor a cero");
  }

  @ParameterizedTest
  @DisplayName("Should reject amounts with more than two decimals")
  @ValueSource(strings = {"0.001", "1.005", "10.555"})
  void shouldRejectMoreThanTwoDecimals(String value) {
    assertThatThrownBy(() -> MoneyRules.requireValidAmount(new BigDecimal(value), "El monto"))
      .isInstanceOf(DomainException.class).hasMessageContaining("decimales");
  }

  @ParameterizedTest
  @DisplayName("Should reject amounts with more than ten integer digits")
  @ValueSource(strings = {"10000000000", "10000000000.00", "99999999999.99"})
  void shouldRejectMoreThanTenIntegerDigits(String value) {
    assertThatThrownBy(() -> MoneyRules.requireValidAmount(new BigDecimal(value), "El monto"))
      .isInstanceOf(DomainException.class).hasMessageContaining("dígitos enteros");
  }
}
