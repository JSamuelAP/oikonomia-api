package dev.jsamuelap.oikonomiaapi.budget.application.service;

import java.math.BigDecimal;
import java.time.YearMonth;
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
import dev.jsamuelap.oikonomiaapi.budget.domain.port.in.DeleteMonthlyBudgetUseCase;
import dev.jsamuelap.oikonomiaapi.budget.domain.port.in.GetMonthlyBudgetUseCase;
import dev.jsamuelap.oikonomiaapi.budget.domain.port.in.ListMonthlyBudgetUseCase;
import dev.jsamuelap.oikonomiaapi.budget.domain.port.in.MonthlyBudgetDetailView;
import dev.jsamuelap.oikonomiaapi.budget.domain.port.in.MonthlyBudgetView;
import dev.jsamuelap.oikonomiaapi.budget.domain.port.in.UpdateMonthlyBudgetCommand;
import dev.jsamuelap.oikonomiaapi.budget.domain.port.in.UpdateMonthlyBudgetUseCase;
import dev.jsamuelap.oikonomiaapi.budget.domain.port.out.CategoryLookupPort;
import dev.jsamuelap.oikonomiaapi.budget.domain.port.out.CategorySummary;
import dev.jsamuelap.oikonomiaapi.budget.domain.port.out.MonthlyBudgetDetail;
import dev.jsamuelap.oikonomiaapi.budget.domain.port.out.MonthlyBudgetRepositoryPort;
import dev.jsamuelap.oikonomiaapi.budget.domain.port.out.TransactionTotalsLookupPort;
import dev.jsamuelap.oikonomiaapi.shared.domain.exception.DomainException;
import dev.jsamuelap.oikonomiaapi.shared.domain.validation.MoneyRules;
import dev.jsamuelap.oikonomiaapi.shared.domain.validation.PeriodRules;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MonthlyBudgetService
  implements
    ListMonthlyBudgetUseCase,
    GetMonthlyBudgetUseCase,
    CreateMonthlyBudgetUseCase,
    UpdateMonthlyBudgetUseCase,
    DeleteMonthlyBudgetUseCase {
  private final MonthlyBudgetRepositoryPort monthlyBudgetRepository;
  private final CategoryLookupPort categoryLookupPort;
  private final TransactionTotalsLookupPort transactionTotalsLookupPort;

  @Override
  @Transactional(readOnly = true)
  public List<MonthlyBudgetView> getAll(UUID userId, YearMonth yearMonth) {
    YearMonth effectiveYearMonth = Objects.requireNonNullElse(yearMonth, YearMonth.now());
    PeriodRules.requireSupportedYear(effectiveYearMonth);

    List<MonthlyBudget> budgets = monthlyBudgetRepository.findAllByUser(userId, effectiveYearMonth);
    if (budgets.isEmpty()) {
      return List.of();
    }

    Set<UUID> categoryIds = budgets.stream().map(MonthlyBudget::getCategoryId).collect(Collectors.toSet());
    Map<UUID, CategorySummary> categories = categoryLookupPort.findByIds(categoryIds, userId);

    Map<UUID, BigDecimal> totals = transactionTotalsLookupPort.findTotalsByCategory(userId, effectiveYearMonth);

    return budgets.stream()
      .map(b -> new MonthlyBudgetView(b.getId(), b.getUserId(), b.getMonth(), b.getYear(), b.getExpectedAmount(),
        totals.getOrDefault(b.getCategoryId(), MoneyRules.ZERO_AMOUNT), requireCategory(categories, b.getCategoryId())))
      .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public MonthlyBudgetDetailView getById(UUID id, UUID userId) {
    MonthlyBudgetDetail budget = monthlyBudgetRepository.findDetailByIdAndUser(id, userId)
      .orElseThrow(() -> new MonthlyBudgetNotFoundException(id));

    CategorySummary category = requireCategory(categoryLookupPort.findByIds(Set.of(budget.categoryId()), userId),
      budget.categoryId());

    // Consultar el mes completo es irrelevante. Buscar por categoría requeriría
    // otro índice
    Map<UUID, BigDecimal> totals = transactionTotalsLookupPort.findTotalsByCategory(userId,
      YearMonth.of(budget.year(), budget.month()));

    return new MonthlyBudgetDetailView(budget.id(), budget.month(), budget.year(), budget.expectedAmount(),
      totals.getOrDefault(budget.categoryId(), MoneyRules.ZERO_AMOUNT), category, budget.createdAt(),
      budget.updatedAt());
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

  @Override
  @Transactional
  public void deleteById(UUID id, UUID userId) {
    MonthlyBudget budget = monthlyBudgetRepository.findByIdAndUser(id, userId)
      .orElseThrow(() -> new MonthlyBudgetNotFoundException(id));
    if (!budget.isDeleted()) {
      budget.delete();
      monthlyBudgetRepository.save(budget);
    }
  }

  private static CategorySummary requireCategory(Map<UUID, CategorySummary> categories, UUID id) {
    CategorySummary category = categories.get(id);
    if (category == null) {
      throw new IllegalStateException("La categoría %s de un presupuesto no existe".formatted(id));
    }
    return category;
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
