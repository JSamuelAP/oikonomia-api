package dev.jsamuelap.oikonomiaapi.category.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.jsamuelap.oikonomiaapi.category.domain.exception.CategoryAlreadyExistsException;
import dev.jsamuelap.oikonomiaapi.category.domain.exception.CategoryNotFoundException;
import dev.jsamuelap.oikonomiaapi.category.domain.model.Category;
import dev.jsamuelap.oikonomiaapi.category.domain.model.FlowType;
import dev.jsamuelap.oikonomiaapi.category.domain.port.in.CategoryDetail;
import dev.jsamuelap.oikonomiaapi.category.domain.port.in.CategoryView;
import dev.jsamuelap.oikonomiaapi.category.domain.port.in.CreateCategoryCommand;
import dev.jsamuelap.oikonomiaapi.category.domain.port.out.CategoryRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("Category service")
class CategoryServiceTest {
  @Mock
  private CategoryRepository categoryRepository;

  @InjectMocks
  private CategoryService categoryService;

  @Nested
  @DisplayName("Get category by ID")
  class GetById {
    @Test
    @DisplayName("Should reject when category does not exist")
    void shouldRejectWhenCategoryDoesNotExist() {
      UUID id = UUID.randomUUID();
      UUID userId = UUID.randomUUID();
      when(categoryRepository.findDetailByIdAndUser(id, userId)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> categoryService.getById(id, userId)).isInstanceOf(CategoryNotFoundException.class)
        .hasMessageContaining(id.toString());
    }

    @Test
    @DisplayName("Should return category detail")
    void shouldReturnCategoryDetail() {
      UUID id = UUID.randomUUID();
      UUID userId = UUID.randomUUID();
      CategoryDetail category = new CategoryDetail(id, "Groceries", FlowType.EXPENSE, Instant.now(), Instant.now());
      when(categoryRepository.findDetailByIdAndUser(id, userId)).thenReturn(Optional.of(category));

      CategoryDetail response = categoryService.getById(id, userId);
      assertThat(response).isEqualTo(category);
    }
  }

  @Nested
  @DisplayName("Get categories by IDs")
  class GetByIds {
    @Test
    @DisplayName("Should return empty list when no IDs provided")
    void shouldReturnEmptyForEmptyIds() {
      UUID userId = UUID.randomUUID();

      when(categoryRepository.findByIdsAndUser(Set.of(), userId)).thenReturn(List.of());

      var result = categoryService.getByIds(Set.of(), userId);

      assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should map categories to view correctly")
    void shouldMapCategoriesToView() {
      UUID userId = UUID.randomUUID();
      UUID categoryId = UUID.randomUUID();
      Category category = Category.reconstitute(categoryId, userId, "Groceries", FlowType.EXPENSE, null);
      when(categoryRepository.findByIdsAndUser(Set.of(categoryId), userId)).thenReturn(List.of(category));

      var result = categoryService.getByIds(Set.of(categoryId), userId);
      assertThat(result).hasSize(1);

      CategoryView view = result.getFirst();
      assertThat(view.id()).isEqualTo(categoryId);
      assertThat(view.name()).isEqualTo("Groceries");
      assertThat(view.flowType()).isEqualTo("EXPENSE");
      assertThat(view.deleted()).isFalse();
    }
  }

  @Nested
  @DisplayName("Create category")
  class Create {
    @Test
    @DisplayName("Should reject when category already exists")
    void shouldRejectWhenCategoryAlreadyExists() {
      var command = new CreateCategoryCommand(UUID.randomUUID(), "Groceries", FlowType.EXPENSE);
      when(categoryRepository.existsByNameAndFlowTypeAndUserId("Groceries", FlowType.EXPENSE, command.userId()))
        .thenReturn(true);

      assertThatThrownBy(() -> categoryService.createCategory(command))
        .isInstanceOf(CategoryAlreadyExistsException.class).hasMessageContaining("Ya existe una categoría");

      verify(categoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should persist category")
    void shouldPersistCategory() {
      UUID id = UUID.randomUUID();
      UUID userId = UUID.randomUUID();
      Category savedCategory = Category.reconstitute(id, userId, "Groceries", FlowType.EXPENSE, null);
      when(categoryRepository.existsByNameAndFlowTypeAndUserId(any(), any(), any())).thenReturn(false);
      when(categoryRepository.save(any())).thenReturn(savedCategory);

      var command = new CreateCategoryCommand(userId, "Groceries", FlowType.EXPENSE);
      UUID savedId = categoryService.createCategory(command);

      assertThat(savedId).isNotNull();

      verify(categoryRepository).save(argThat(category -> category.getUserId().equals(userId)));
    }
  }

  @Nested
  @DisplayName("Delete category")
  class Delete {
    @Test
    @DisplayName("Should reject when category does not exist")
    void shouldRejectWhenCategoryDoesNotExist() {
      UUID id = UUID.randomUUID();
      UUID userId = UUID.randomUUID();
      when(categoryRepository.findByIdAndUser(id, userId)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> categoryService.deleteById(id, userId)).isInstanceOf(CategoryNotFoundException.class)
        .hasMessageContaining(id.toString());

      verify(categoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should not delete when category is already deleted")
    void shouldNotDeleteWhenCategoryIsAlreadyDeleted() {
      UUID id = UUID.randomUUID();
      UUID userId = UUID.randomUUID();
      Category deletedCategory = Category.reconstitute(id, userId, "Groceries", FlowType.EXPENSE, Instant.now());
      when(categoryRepository.findByIdAndUser(id, userId)).thenReturn(Optional.of(deletedCategory));

      categoryService.deleteById(id, userId);

      verify(categoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should delete category")
    void shouldDelete() {
      UUID id = UUID.randomUUID();
      UUID userId = UUID.randomUUID();
      Category existingCategory = Category.reconstitute(id, userId, "Groceries", FlowType.EXPENSE, null);
      when(categoryRepository.findByIdAndUser(any(), any())).thenReturn(Optional.of(existingCategory));

      categoryService.deleteById(id, userId);

      verify(categoryRepository).save(argThat(category -> category.getId().equals(id) && category.isDeleted()));
    }
  }
}
