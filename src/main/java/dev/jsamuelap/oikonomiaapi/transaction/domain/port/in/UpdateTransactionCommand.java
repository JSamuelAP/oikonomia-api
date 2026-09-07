package dev.jsamuelap.oikonomiaapi.transaction.domain.port.in;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record UpdateTransactionCommand(UUID id, UUID userId, UUID categoryId, BigDecimal amount, LocalDate date,
  String notes) {
}
