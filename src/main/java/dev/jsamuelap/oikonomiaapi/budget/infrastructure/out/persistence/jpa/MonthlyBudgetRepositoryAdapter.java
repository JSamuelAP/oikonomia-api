package dev.jsamuelap.oikonomiaapi.budget.infrastructure.out.persistence.jpa;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import dev.jsamuelap.oikonomiaapi.budget.domain.model.MonthlyBudget;
import dev.jsamuelap.oikonomiaapi.budget.domain.port.out.MonthlyBudgetRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MonthlyBudgetRepositoryAdapter implements MonthlyBudgetRepository {
  private final MonthlyBudgetJpaRepository jpaRepository;
  private final MonthlyBudgetPersistenceMapper mapper;

  @Override
  public List<MonthlyBudget> findAllByUser(UUID userId, YearMonth yearMonth) {
    Short month = (short) yearMonth.getMonthValue();
    Short year = (short) yearMonth.getYear();
    return jpaRepository.findByUserIdAndMonthAndYearAndDeletedAtIsNull(userId, month, year).stream()
      .map(mapper::toDomain).toList();
  }

  @Override
  public Optional<MonthlyBudget> findByIdAndUser(UUID id, UUID userId) {
    return jpaRepository.findByIdAndUserIdAndDeletedAtIsNull(id, userId).map(mapper::toDomain);
  }

  @Override
  public boolean existsByCategoryAndUserAndDate(UUID categoryId, UUID userId, Short month, Short year) {
    return jpaRepository.existsByCategoryIdAndUserIdAndMonthAndYearAndDeletedAtIsNull(categoryId, userId, month, year);
  }

  @Override
  public MonthlyBudget save(MonthlyBudget budget) {
    MonthlyBudgetJpaEntity entity = jpaRepository.findById(budget.getId())
      .map(existing -> updateEntity(existing, budget)).orElseGet(() -> mapper.toEntity(budget));
    MonthlyBudgetJpaEntity saved = jpaRepository.save(entity);
    return mapper.toDomain(saved);
  }

  private MonthlyBudgetJpaEntity updateEntity(MonthlyBudgetJpaEntity entity, MonthlyBudget budget) {
    entity.setCategoryId(budget.getCategoryId());
    entity.setMonth(budget.getMonth());
    entity.setYear(budget.getYear());
    entity.setExpectedAmount(budget.getExpectedAmount());
    return entity;
  }
}
