package dev.jsamuelap.oikonomiaapi.category.infrastructure.in.rest;

import static dev.jsamuelap.oikonomiaapi.shared.util.StringSanitizer.trimOrNull;

import dev.jsamuelap.oikonomiaapi.category.domain.model.FlowType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Category creation request")
public record CreateCategoryRequest(
  // spotless:off
  @NotBlank(message = "El nombre es requerido")
  @Size(min = 2, max = 50, message = "El nombre debe tener entre {min} y {max} catecteres de longitud")
  @Schema(description = "Category name", example = "Groceries")
  String name,

  @NotNull(message = "El tipo de flujo es requerido")
  @Schema(description = "Category flow type", example = "EXPENSE", allowableValues = {"INCOME", "EXPENSE"})
  FlowType flowType
  // spotless:on
  ) {
  public CreateCategoryRequest {
    name = trimOrNull(name);
  }
}
