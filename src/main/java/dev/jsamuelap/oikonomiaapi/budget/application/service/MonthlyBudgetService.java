package dev.jsamuelap.oikonomiaapi.budget.application.service;

import java.time.Year;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.jsamuelap.oikonomiaapi.budget.domain.exception.MonthlyBudgetAlreadyExistsException;
import dev.jsamuelap.oikonomiaapi.budget.domain.exception.MonthlyBudgetNotFoundException;
import dev.jsamuelap.oikonomiaapi.budget.domain.model.MonthlyBudget;
import dev.jsamuelap.oikonomiaapi.budget.domain.port.in.CreateMonthlyBudgetCommand;
import dev.jsamuelap.oikonomiaapi.budget.domain.port.in.CreateMonthlyBudgetUseCase;
import dev.jsamuelap.oikonomiaapi.budget.domain.port.in.GetMonthlyBudgetUseCase;
import dev.jsamuelap.oikonomiaapi.budget.domain.port.in.ListMonthlyBudgetUseCase;
import dev.jsamuelap.oikonomiaapi.budget.domain.port.in.MonthlyBudgetView;
import dev.jsamuelap.oikonomiaapi.budget.domain.port.in.UpdateMonthlyBudgetCommand;
import dev.jsamuelap.oikonomiaapi.budget.domain.port.in.UpdateMonthlyBudgetUseCase;
import dev.jsamuelap.oikonomiaapi.budget.domain.port.out.CategoryLookupPort;
import dev.jsamuelap.oikonomiaapi.budget.domain.port.out.CategorySummary;
import dev.jsamuelap.oikonomiaapi.budget.domain.port.out.MonthlyBudgetRepository;
import dev.jsamuelap.oikonomiaapi.shared.domain.exception.DomainException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MonthlyBudgetService
  implements
    ListMonthlyBudgetUseCase,
    GetMonthlyBudgetUseCase,
    CreateMonthlyBudgetUseCase,
    UpdateMonthlyBudgetUseCase {
  private final MonthlyBudgetRepository monthlyBudgetRepository;
  private final CategoryLookupPort categoryLookupPort;

  @Override
  @Transactional(readOnly = true)
  public List<MonthlyBudgetView> getAll(UUID userId, Short year) {
    Short effectiveYear = Objects.requireNonNullElse(year, (short) Year.now().getValue());
    if (effectiveYear < 2025 || effectiveYear > 2100) {
      throw new DomainException("El año debe ser entre 2025 y 2100");
    }

    List<MonthlyBudget> budgets = monthlyBudgetRepository.findAllByUser(userId, effectiveYear);

    Set<UUID> categoryIds = budgets.stream().map(MonthlyBudget::getCategoryId).collect(Collectors.toSet());
    Map<UUID, CategorySummary> categories = categoryLookupPort.findByIds(categoryIds, userId);

    return budgets.stream().map(b -> new MonthlyBudgetView(b.getId(), b.getUserId(), b.getMonth(), b.getYear(),
      b.getExpectedAmount(), categories.get(b.getCategoryId()))).toList();
  }

  @Override
  @Transactional(readOnly = true)
  public MonthlyBudgetView getById(UUID id, UUID userId) {
    MonthlyBudget budget = monthlyBudgetRepository.findByIdAndUser(id, userId)
      .orElseThrow(() -> new MonthlyBudgetNotFoundException(id));

    CategorySummary category = categoryLookupPort.findByIds(Set.of(budget.getCategoryId()), userId)
      .get(budget.getCategoryId());

    return new MonthlyBudgetView(budget.getId(), budget.getUserId(), budget.getMonth(), budget.getYear(),
      budget.getExpectedAmount(), category);
  }

  @Override
  @Transactional
  public UUID create(CreateMonthlyBudgetCommand command) {
    CategorySummary category = getCategory(command.categoryId(), command.userId());

    validateMonthlyBudgetAlreadyExists(command.categoryId(), command.userId(), command.month(), command.year(),
      category.name());

    MonthlyBudget monthlyBudget = MonthlyBudget.create(command.userId(), command.categoryId(), command.month(),
      command.year(), command.expectedAmount());
    MonthlyBudget saved = monthlyBudgetRepository.save(monthlyBudget);
    return saved.getId();
  }

  @Override
  public void update(UpdateMonthlyBudgetCommand command) {
    MonthlyBudget budget = monthlyBudgetRepository.findByIdAndUser(command.id(), command.userId())
      .orElseThrow(() -> new MonthlyBudgetNotFoundException(command.id()));

    CategorySummary category = getCategory(command.categoryId(), command.userId());

    boolean categoryChanged = !budget.getCategoryId().equals(command.categoryId());
    boolean monthChanged = !budget.getMonth().equals(command.month());
    boolean yearChanged = !budget.getYear().equals(command.year());
    if (categoryChanged || monthChanged || yearChanged) {
      validateMonthlyBudgetAlreadyExists(command.categoryId(), command.userId(), command.month(), command.year(),
        category.name());
    }

    budget.changeCategoryId(command.categoryId());
    budget.changeMonth(command.month());
    budget.changeYear(command.year());
    budget.changeExpectedAmount(command.expectedAmount());

    monthlyBudgetRepository.save(budget);
  }

  private CategorySummary getCategory(UUID categoryId, UUID userId) {
    Map<UUID, CategorySummary> categories = categoryLookupPort.findByIds(Set.of(categoryId), userId);
    if (!categories.containsKey(categoryId) || categories.get(categoryId).deleted()) {
      throw new DomainException("Categoría no encontrada");
    }
    return categories.get(categoryId);
  }

  private void validateMonthlyBudgetAlreadyExists(UUID categoryId, UUID userId, Short month, Short year,
    String categoryName) {
    if (monthlyBudgetRepository.existsByCategoryAndUserAndDate(categoryId, userId, month, year)) {
      throw new MonthlyBudgetAlreadyExistsException(categoryName, month, year);
    }
  }
}
