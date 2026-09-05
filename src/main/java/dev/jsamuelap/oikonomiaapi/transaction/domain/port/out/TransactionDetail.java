package dev.jsamuelap.oikonomiaapi.transaction.domain.port.out;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record TransactionDetail(UUID id, UUID categoryId, BigDecimal amount, LocalDate date, String notes,
  Instant createdAt, Instant updatedAt) {
}
