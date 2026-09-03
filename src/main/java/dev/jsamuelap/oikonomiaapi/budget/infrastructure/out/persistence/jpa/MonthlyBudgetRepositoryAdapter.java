package dev.jsamuelap.oikonomiaapi.budget.infrastructure.out.persistence.jpa;

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
  public List<MonthlyBudget> findAllByUser(UUID userId, Short year) {
    return jpaRepository.findByUserIdAndYearAndDeletedAtIsNull(userId, year).stream().map(mapper::toDomain).toList();
  }

  @Override
  public Optional<MonthlyBudget> findByIdAndUser(UUID id, UUID userId) {
    return jpaRepository.findByIdAndUserIdAndDeletedAtIsNull(id, userId).map(mapper::toDomain);
  }

  @Override
  public MonthlyBudget save(MonthlyBudget budget) {
    MonthlyBudgetJpaEntity entity = mapper.toEntity(budget);
    MonthlyBudgetJpaEntity saved = jpaRepository.save(entity);
    return mapper.toDomain(saved);
  }
}
