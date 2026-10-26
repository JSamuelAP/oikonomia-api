package dev.jsamuelap.oikonomiaapi.shared.domain.validation;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.YearMonth;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import dev.jsamuelap.oikonomiaapi.shared.domain.exception.DomainException;

@DisplayName("Period rules validation")
public class PeriodRulesTest {
  @Test
  @DisplayName("Should reject null month")
  void shouldRejectNull() {
    assertThatThrownBy(() -> PeriodRules.requireSupportedYear(null)).isInstanceOf(DomainException.class)
      .hasMessageContaining("El año es requerido");
  }

  @Test
  @DisplayName("Should accept minimum year")
  void shouldAcceptMinimumYear() {
    assertThatCode(() -> PeriodRules.requireSupportedYear(PeriodRules.MIN_YEAR)).doesNotThrowAnyException();
  }

  @Test
  @DisplayName("Should accept maximum year")
  void shouldAcceptMaximumYear() {
    assertThatCode(() -> PeriodRules.requireSupportedYear(PeriodRules.MAX_YEAR)).doesNotThrowAnyException();
  }

  @ParameterizedTest
  @DisplayName("Should accept years between min and max")
  @ValueSource(ints = {2026, 2050, 2099})
  void shouldAcceptYearsBetweenMinAndMax(int year) {
    assertThatCode(() -> PeriodRules.requireSupportedYear(year)).doesNotThrowAnyException();
  }

  @ParameterizedTest
  @DisplayName("Should reject years below minimum")
  @ValueSource(ints = {PeriodRules.MIN_YEAR - 1, 2000, 0, -1})
  void shouldRejectYearsBelowMinimum(int year) {
    assertThatThrownBy(() -> PeriodRules.requireSupportedYear(year)).isInstanceOf(DomainException.class)
      .hasMessageContaining("El año debe estar entre");
  }

  @ParameterizedTest
  @DisplayName("Should reject years above maximum")
  @ValueSource(ints = {PeriodRules.MAX_YEAR + 1, 2200, 9999})
  void shouldRejectYearsAboveMaximum(int year) {
    assertThatThrownBy(() -> PeriodRules.requireSupportedYear(year)).isInstanceOf(DomainException.class)
      .hasMessageContaining("El año debe estar entre");
  }

  @Test
  @DisplayName("Should delegate YearMonth to year validation")
  void shouldDelegateYearMonthToYearValidation() {
    YearMonth yearMonth = YearMonth.of(PeriodRules.MIN_YEAR, 1);
    assertThatCode(() -> PeriodRules.requireSupportedYear(yearMonth)).doesNotThrowAnyException();
  }
}
