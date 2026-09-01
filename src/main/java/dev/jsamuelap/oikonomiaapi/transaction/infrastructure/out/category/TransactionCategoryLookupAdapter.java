package dev.jsamuelap.oikonomiaapi.transaction.infrastructure.out.category;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import dev.jsamuelap.oikonomiaapi.category.domain.port.in.CategoryView;
import dev.jsamuelap.oikonomiaapi.category.domain.port.in.GetCategoriesByIdsUseCase;
import dev.jsamuelap.oikonomiaapi.transaction.domain.port.out.CategoryLookupPort;
import dev.jsamuelap.oikonomiaapi.transaction.domain.port.out.CategorySummary;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TransactionCategoryLookupAdapter implements CategoryLookupPort {
  private final GetCategoriesByIdsUseCase getCategoriesByIdsUseCase;

  @Override
  public Map<UUID, CategorySummary> findByIds(Set<UUID> categoryIds) {
    return getCategoriesByIdsUseCase.getByIds(categoryIds).stream().collect(
      Collectors.toMap(CategoryView::id, view -> new CategorySummary(view.id(), view.name(), view.flowType().name())));
  }
}
