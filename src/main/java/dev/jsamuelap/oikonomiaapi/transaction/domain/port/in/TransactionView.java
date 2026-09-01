package dev.jsamuelap.oikonomiaapi.transaction.domain.port.in;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import dev.jsamuelap.oikonomiaapi.transaction.domain.port.out.CategorySummary;

public record TransactionView(UUID id, BigDecimal amount, LocalDate date, String notes, CategorySummary category) {
}
