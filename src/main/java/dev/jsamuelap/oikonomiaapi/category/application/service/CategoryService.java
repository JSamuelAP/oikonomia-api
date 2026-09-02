package dev.jsamuelap.oikonomiaapi.category.application.service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.jsamuelap.oikonomiaapi.category.domain.exception.CategoryNotFoundException;
import dev.jsamuelap.oikonomiaapi.category.domain.model.Category;
import dev.jsamuelap.oikonomiaapi.category.domain.port.in.CategoryView;
import dev.jsamuelap.oikonomiaapi.category.domain.port.in.CreateCategoryCommand;
import dev.jsamuelap.oikonomiaapi.category.domain.port.in.CreateCategoryUseCase;
import dev.jsamuelap.oikonomiaapi.category.domain.port.in.GetCategoriesByIdsUseCase;
import dev.jsamuelap.oikonomiaapi.category.domain.port.in.GetCategoryUseCase;
import dev.jsamuelap.oikonomiaapi.category.domain.port.in.ListCategoriesUseCase;
import dev.jsamuelap.oikonomiaapi.category.domain.port.out.CategoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryService
  implements
    ListCategoriesUseCase,
    GetCategoryUseCase,
    GetCategoriesByIdsUseCase,
    CreateCategoryUseCase {
  private final CategoryRepository categoryRepository;

  @Override
  public List<Category> getAll(UUID userId) {
    return categoryRepository.findByUser(userId);
  }

  @Override
  @Transactional(readOnly = true)
  public Category getById(UUID categoryId, UUID userId) {
    return categoryRepository.findByIdAndUser(categoryId, userId)
      .orElseThrow(() -> new CategoryNotFoundException(categoryId));
  }

  @Override
  @Transactional(readOnly = true)
  public List<CategoryView> getByIds(Set<UUID> categoryIds) {
    return categoryRepository.findAllById(categoryIds).stream()
      .map(c -> new CategoryView(c.getId(), c.getName(), c.getFlowType())).toList();
  }

  @Override
  @Transactional
  public UUID createCategory(CreateCategoryCommand command) {
    Category category = Category.create(command.userId(), command.name(), command.flowType());
    Category saved = categoryRepository.save(category);
    return saved.getId();
  }
}
