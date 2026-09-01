package dev.jsamuelap.oikonomiaapi.budget.infrastructure.in.rest;

import java.util.List;
import java.util.UUID;

import org.mapstruct.Mapper;

import dev.jsamuelap.oikonomiaapi.budget.domain.port.in.CreateMonthlyBudgetCommand;
import dev.jsamuelap.oikonomiaapi.budget.domain.port.in.MonthlyBudgetView;

@Mapper(componentModel = "spring")
public interface MonthlyBudgetRestMapper {
  List<MonthlyBudgetResponse> toResponse(List<MonthlyBudgetView> monthlyBudgets);

  MonthlyBudgetResponse toResponse(MonthlyBudgetView monthlyBudgets);

  CreateMonthlyBudgetCommand toCommand(CreateMonthlyBudgetRequest createMonthlyBudgetRequest, UUID userId);
}
