package dev.jsamuelap.oikonomiaapi.transaction.infrastructure.in.rest;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record TransactionDetailResponse(UUID id, BigDecimal amount, LocalDate date, String notes,
  CategorySummaryResponse category, Instant createdAt, Instant updatedAt) {
}
