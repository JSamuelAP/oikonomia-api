package dev.jsamuelap.oikonomiaapi.budget.infrastructure.out.transaction;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Component;

import dev.jsamuelap.oikonomiaapi.budget.domain.port.out.TransactionTotalsLookupPort;
import dev.jsamuelap.oikonomiaapi.transaction.domain.port.in.GetMonthlyTotalsByCategoryUseCase;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TransactionTotalsLookupAdapter implements TransactionTotalsLookupPort {
  private final GetMonthlyTotalsByCategoryUseCase getMonthlyTotalsByCategoryUseCase;

  @Override
  public Map<UUID, BigDecimal> findTotalsByCategory(UUID userId, YearMonth month) {
    return getMonthlyTotalsByCategoryUseCase.getTotalsByCategory(userId, month);
  }
}
