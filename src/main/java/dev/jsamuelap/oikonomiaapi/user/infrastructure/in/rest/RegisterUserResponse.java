package dev.jsamuelap.oikonomiaapi.user.infrastructure.in.rest;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "User registration response")
public record RegisterUserResponse(
  @Schema(description = "Created user ID", example = "123e4567-e89b-12d3-a456-426614174000") UUID userId) {
}
