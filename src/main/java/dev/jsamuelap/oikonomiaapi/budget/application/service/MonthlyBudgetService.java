package dev.jsamuelap.oikonomiaapi.budget.application.service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.jsamuelap.oikonomiaapi.budget.domain.exception.MonthlyBudgetNotFoundException;
import dev.jsamuelap.oikonomiaapi.budget.domain.model.MonthlyBudget;
import dev.jsamuelap.oikonomiaapi.budget.domain.port.in.CreateMonthlyBudgetCommand;
import dev.jsamuelap.oikonomiaapi.budget.domain.port.in.CreateMonthlyBudgetUseCase;
import dev.jsamuelap.oikonomiaapi.budget.domain.port.in.GetMonthlyBudgetUseCase;
import dev.jsamuelap.oikonomiaapi.budget.domain.port.in.ListMonthlyBudgetUseCase;
import dev.jsamuelap.oikonomiaapi.budget.domain.port.in.MonthlyBudgetView;
import dev.jsamuelap.oikonomiaapi.budget.domain.port.out.CategoryLookupPort;
import dev.jsamuelap.oikonomiaapi.budget.domain.port.out.CategorySummary;
import dev.jsamuelap.oikonomiaapi.budget.domain.port.out.MonthlyBudgetRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MonthlyBudgetService
  implements
    ListMonthlyBudgetUseCase,
    GetMonthlyBudgetUseCase,
    CreateMonthlyBudgetUseCase {
  private final MonthlyBudgetRepository monthlyBudgetRepository;
  private final CategoryLookupPort categoryLookupPort;

  @Override
  @Transactional(readOnly = true)
  public List<MonthlyBudgetView> getAll(UUID userId) {
    List<MonthlyBudget> budgets = monthlyBudgetRepository.findAllByUser(userId);

    Set<UUID> categoryIds = budgets.stream().map(MonthlyBudget::getCategoryId).collect(Collectors.toSet());
    Map<UUID, CategorySummary> categories = categoryLookupPort.findByIds(categoryIds);

    return budgets.stream().map(b -> new MonthlyBudgetView(b.getId(), b.getUserId(), b.getMonth(), b.getYear(),
      b.getExpectedAmount(), categories.get(b.getCategoryId()))).toList();
  }

  @Override
  @Transactional(readOnly = true)
  public MonthlyBudgetView getById(UUID id, UUID userId) {
    MonthlyBudget budget = monthlyBudgetRepository.findByIdAndUser(id, userId)
      .orElseThrow(() -> new MonthlyBudgetNotFoundException(id));

    CategorySummary category = categoryLookupPort.findByIds(Set.of(budget.getCategoryId())).get(budget.getCategoryId());

    return new MonthlyBudgetView(budget.getId(), budget.getUserId(), budget.getMonth(), budget.getYear(),
      budget.getExpectedAmount(), category);
  }

  @Override
  @Transactional
  public UUID create(CreateMonthlyBudgetCommand command) {
    MonthlyBudget monthlyBudget = MonthlyBudget.create(command.userId(), command.categoryId(), command.month(),
      command.year(), command.expectedAmount());
    MonthlyBudget saved = monthlyBudgetRepository.save(monthlyBudget);
    return saved.getId();
  }
}
