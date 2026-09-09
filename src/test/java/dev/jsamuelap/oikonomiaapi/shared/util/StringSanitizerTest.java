package dev.jsamuelap.oikonomiaapi.shared.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("String sanitizer utility")
public class StringSanitizerTest {

  @Nested
  @DisplayName("trimOrNull")
  class TrimOrNull {
    @Test
    @DisplayName("Should return trimmed string for normal input")
    void shouldTrimNormalString() {
      assertThat(StringSanitizer.trimOrNull("  hello  ")).isEqualTo("hello");
    }

    @ParameterizedTest
    @DisplayName("Should return empty string for blank inputs")
    @ValueSource(strings = {"", " ", "  ", "\t", "\n", " \t\n "})
    void shouldReturnEmptyForBlankInputs(String input) {
      assertThat(StringSanitizer.trimOrNull(input)).isEmpty();
    }

    @Test
    @DisplayName("Should return null for null input")
    void shouldReturnNullForNull() {
      assertThat(StringSanitizer.trimOrNull(null)).isNull();
    }

    @Test
    @DisplayName("Should handle tabs and newlines")
    void shouldHandleTabsAndNewlines() {
      assertThat(StringSanitizer.trimOrNull("\t\n  test  \n\t")).isEqualTo("test");
    }
  }

  @Nested
  @DisplayName("normalizeEmail")
  class NormalizeEmail {
    @ParameterizedTest
    @DisplayName("Should normalize to lowercase trimmed email")
    @ValueSource(strings = {"TEST@EXAMPLE.COM", "Test@Example.Com", "  TEST@EXAMPLE.COM  "})
    void shouldNormalizeEmail(String input) {
      assertThat(StringSanitizer.normalizeEmail(input)).isEqualTo("test@example.com");
    }

    @ParameterizedTest
    @DisplayName("Should return empty string for blank inputs")
    @ValueSource(strings = {"", " ", "  ", "\t", "\n"})
    void shouldReturnEmptyForBlankInputs(String input) {
      assertThat(StringSanitizer.normalizeEmail(input)).isEmpty();
    }

    @Test
    @DisplayName("Should return null for null input")
    void shouldReturnNullForNull() {
      assertThat(StringSanitizer.normalizeEmail(null)).isNull();
    }
  }
}
