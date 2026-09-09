package dev.jsamuelap.oikonomiaapi.category.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("Flow type enum")
public class FlowTypeTest {

  @Nested
  @DisplayName("forValue factory method")
  class ForValue {
    @ParameterizedTest(name = "{0}")
    @DisplayName("Should parse valid flow type (case-insensitive)")
    @ValueSource(strings = {"INCOME", "income", "Income", "EXPENSE", "expense", "Expense"})
    void shouldParseValidFlowType(String value) {
      FlowType result = FlowType.forValue(value);

      assertThat(result).isNotNull();
      assertThat(result.name()).isEqualTo(value.toUpperCase());
    }

    @ParameterizedTest(name = "should reject {0}")
    @DisplayName("Should throw exception for invalid flow type")
    @ValueSource(strings = {"INCOMES", "ingreso", "gasto", "", "   "})
    void shouldRejectInvalidFlowType(String invalidValue) {
      assertThatThrownBy(() -> FlowType.forValue(invalidValue)).isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Tipo de flujo inválido");
    }

    @Test
    @DisplayName("Should throw exception for null input")
    void shouldRejectNullInput() {
      assertThatThrownBy(() -> FlowType.forValue(null)).isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Tipo de flujo inválido").hasMessageContaining("null");
    }
  }
}
