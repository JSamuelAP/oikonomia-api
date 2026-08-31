package dev.jsamuelap.oikonomiaapi.category.domain.model;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum FlowType {
  INCOME, EXPENSE;

  // En dominio no debería aceptarse @JsonCreator, pero se incluye porque aporta
  // mucho valor al cliente y no afecta
  // casi nada en la arquitectura
  @JsonCreator
  public static FlowType forValue(String value) {
    for (FlowType type : FlowType.values()) {
      if (type.name().equalsIgnoreCase(value)) {
        return type;
      }
    }
    throw new IllegalArgumentException(
      "Tipo de flujo inválido: '%s'. Valores permitidos: INCOME, EXPENSE ".formatted(value));
  }
}
