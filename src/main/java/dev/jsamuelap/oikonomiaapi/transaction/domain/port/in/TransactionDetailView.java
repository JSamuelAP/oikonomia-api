package dev.jsamuelap.oikonomiaapi.transaction.domain.port.in;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import dev.jsamuelap.oikonomiaapi.transaction.domain.port.out.CategorySummary;

public record TransactionDetailView(UUID id, BigDecimal amount, LocalDate date, String notes, CategorySummary category,
  Instant createdAt, Instant updatedAt) {
}
