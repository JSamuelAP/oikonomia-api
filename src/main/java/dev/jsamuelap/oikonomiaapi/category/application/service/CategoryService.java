package dev.jsamuelap.oikonomiaapi.category.application.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import dev.jsamuelap.oikonomiaapi.category.domain.exception.CategoryNotFoundException;
import dev.jsamuelap.oikonomiaapi.category.domain.model.Category;
import dev.jsamuelap.oikonomiaapi.category.domain.port.in.CreateCategoryCommand;
import dev.jsamuelap.oikonomiaapi.category.domain.port.in.CreateCategoryUseCase;
import dev.jsamuelap.oikonomiaapi.category.domain.port.in.GetCategoryUseCase;
import dev.jsamuelap.oikonomiaapi.category.domain.port.in.ListCategoriesUseCase;
import dev.jsamuelap.oikonomiaapi.category.domain.port.out.CategoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryService implements ListCategoriesUseCase, GetCategoryUseCase, CreateCategoryUseCase {
  private final CategoryRepository categoryRepository;

  @Override
  public List<Category> getAll(UUID userId) {
    return categoryRepository.findByUser(userId);
  }

  @Override
  public Category getById(UUID categoryId, UUID userId) {
    return categoryRepository.findByIdAndUser(categoryId, userId)
      .orElseThrow(() -> new CategoryNotFoundException(categoryId));
  }

  @Override
  public UUID createCategory(CreateCategoryCommand command) {
    Category category = Category.create(command.userId(), command.name(), command.flowType());
    Category saved = categoryRepository.save(category);
    return saved.getId();
  }
}
