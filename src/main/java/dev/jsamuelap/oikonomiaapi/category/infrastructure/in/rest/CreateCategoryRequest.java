package dev.jsamuelap.oikonomiaapi.category.infrastructure.in.rest;

import static dev.jsamuelap.oikonomiaapi.shared.util.StringSanitizer.trimOrNull;

import dev.jsamuelap.oikonomiaapi.category.domain.model.FlowType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateCategoryRequest(
  // spotless:off
  @NotBlank(message = "El nombre es requerido")
  @Size(min = 2, max = 50, message = "El nombre debe tener entre {min} y {max} catecteres de longitud")
  String name,

  @NotNull(message = "El tipo de flujo es requerido")
  FlowType flowType
  // spotless:on
) {
  public CreateCategoryRequest {
    name = trimOrNull(name);
  }
}
