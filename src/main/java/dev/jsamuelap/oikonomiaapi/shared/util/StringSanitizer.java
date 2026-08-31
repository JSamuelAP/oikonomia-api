package dev.jsamuelap.oikonomiaapi.shared.util;

import java.util.Locale;

public final class StringSanitizer {
  private StringSanitizer() {}

  public static String trimOrNull(String value) {
    return value != null ? value.trim() : null;
  }

  public static String normalizeEmail(String email) {
    return email != null ? email.trim().toLowerCase(Locale.ROOT) : null;
  }
}
