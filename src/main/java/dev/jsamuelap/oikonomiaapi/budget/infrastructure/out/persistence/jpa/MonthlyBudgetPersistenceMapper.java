package dev.jsamuelap.oikonomiaapi.budget.infrastructure.out.persistence.jpa;

import org.mapstruct.Mapper;

import dev.jsamuelap.oikonomiaapi.budget.domain.model.MonthlyBudget;

@Mapper(componentModel = "spring")
public interface MonthlyBudgetPersistenceMapper {
  default MonthlyBudget toDomain(MonthlyBudgetJpaEntity entity) {
    if (entity == null) {
      return null;
    }

    return MonthlyBudget.reconstitute(entity.getId(), entity.getUserId(), entity.getCategoryId(), entity.getMonth(),
      entity.getYear(), entity.getExpectedAmount());
  }

  default MonthlyBudgetJpaEntity toEntity(MonthlyBudget monthlyBudget) {
    if (monthlyBudget == null) {
      return null;
    }

    MonthlyBudgetJpaEntity monthlyBudgetJpaEntity = new MonthlyBudgetJpaEntity();
    monthlyBudgetJpaEntity.setId(monthlyBudget.getId());
    monthlyBudgetJpaEntity.setUserId(monthlyBudget.getUserId());
    monthlyBudgetJpaEntity.setCategoryId(monthlyBudget.getCategoryId());
    monthlyBudgetJpaEntity.setMonth(monthlyBudget.getMonth());
    monthlyBudgetJpaEntity.setYear(monthlyBudget.getYear());
    monthlyBudgetJpaEntity.setExpectedAmount(monthlyBudget.getExpectedAmount());
    return monthlyBudgetJpaEntity;
  }
}
