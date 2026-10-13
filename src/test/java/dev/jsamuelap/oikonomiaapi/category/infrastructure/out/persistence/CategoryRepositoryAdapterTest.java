package dev.jsamuelap.oikonomiaapi.category.infrastructure.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import dev.jsamuelap.oikonomiaapi.category.domain.model.Category;
import dev.jsamuelap.oikonomiaapi.category.domain.model.FlowType;
import dev.jsamuelap.oikonomiaapi.category.domain.port.in.CategoryDetail;
import dev.jsamuelap.oikonomiaapi.category.domain.port.out.CategoryRepositoryPort;
import dev.jsamuelap.oikonomiaapi.support.PersistenceTestSupport;

@DisplayName("Category repository adapter")
public class CategoryRepositoryAdapterTest extends PersistenceTestSupport {
  @Autowired
  private CategoryRepositoryPort categoryRepository;

  @Nested
  @DisplayName("Save category")
  class Save {
    @Test
    @DisplayName("Should persist all fields")
    void shouldPersistAllFields() {
      UUID userId = insertUser();
      Category category = newCategory(userId, "Groceries", FlowType.EXPENSE);

      Category found = categoryRepository.findByIdAndUser(category.getId(), userId).orElseThrow();
      assertThat(found).usingRecursiveComparison().isEqualTo(category);
    }

    @ParameterizedTest
    @DisplayName("Should round trip every flow type")
    @EnumSource(FlowType.class)
    void shouldRoundTripEveryFlowType(FlowType flowType) {
      UUID userId = insertUser();
      Category saved = newCategory(userId, "Any", flowType);

      Category found = categoryRepository.findByIdAndUser(saved.getId(), userId).orElseThrow();

      assertThat(found.getFlowType()).isEqualTo(flowType);
    }

    @Test
    @DisplayName("Should allow recreating category after soft delete")
    void shouldAllowRecreatingCategoryAfterSoftDelete() {
      UUID userId = insertUser();
      deletedCategory(userId, "Groceries", FlowType.EXPENSE);

      assertThatCode(() -> newCategory(userId, "Groceries", FlowType.EXPENSE)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should reject two active categories with same name and flow type")
    void shouldRejectTwoActiveCategoriesWithSameNameAndFlowType() {
      UUID userId = insertUser();
      newCategory(userId, "Groceries", FlowType.EXPENSE);

      assertThatThrownBy(() -> newCategory(userId, "Groceries", FlowType.EXPENSE))
        .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Should allow creating with same name and different flow type")
    void shouldAllowCreatingWithSameNameAndDifferentFlowType() {
      UUID userId = insertUser();
      newCategory(userId, "Any", FlowType.EXPENSE);

      assertThatCode(() -> newCategory(userId, "Any", FlowType.INCOME)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should allow same name and flow type for different users")
    void shouldAllowSameNameAndFlowTypeForDifferentUsers() {
      newCategory(insertUser(), "Groceries", FlowType.EXPENSE);

      assertThatCode(() -> newCategory(insertUser(), "Groceries", FlowType.EXPENSE)).doesNotThrowAnyException();
    }
  }

  @Nested
  @DisplayName("Soft delete category")
  class SoftDelete {
    @Test
    @DisplayName("Should persist deleted at when inserted already deleted")
    void shouldPersistDeletedAtWhenInsertedAlreadyDeleted() {
      Instant deletedAt = Instant.now();
      UUID userId = insertUser();
      Category deleted = Category.reconstitute(UUID.randomUUID(), userId, "Groceries", FlowType.EXPENSE, deletedAt);
      categoryRepository.save(deleted);

      Category found = categoryRepository.findByIdAndUser(deleted.getId(), userId).orElseThrow();
      assertThat(found.isDeleted()).isTrue();
      assertThat(found.getDeletedAt()).isCloseTo(deletedAt, within(1, ChronoUnit.MILLIS));
    }

    @Test
    @DisplayName("Should persist deletion of an existing category")
    void shouldPersistDeletionOfAnExistingCategory() {
      UUID userId = insertUser();
      Category category = deletedCategory(userId, "Groceries", FlowType.EXPENSE);

      Category found = categoryRepository.findByIdAndUser(category.getId(), userId).orElseThrow();
      assertThat(found.isDeleted()).isTrue();
    }
  }

  @Nested
  @DisplayName("Find all by user")
  class FindAllByUser {
    @Test
    @DisplayName("Should return only active of the user")
    void shouldReturnOnlyActiveOfTheUser() {
      UUID userId = insertUser();
      Category active = newCategory(userId, "Groceries", FlowType.EXPENSE);
      deletedCategory(userId, "Any", FlowType.EXPENSE);
      newCategory(insertUser(), "Foreign", FlowType.EXPENSE);

      assertThat(categoryRepository.findByUser(userId)).extracting(Category::getId).containsExactly(active.getId());
    }
  }

  @Nested
  @DisplayName("Find by ID and user")
  class FindByIdAndUser {
    @Test
    @DisplayName("Should not find category of another user")
    void shouldNotFindCategoryOfAnotherUser() {
      Category category = newCategory(insertUser(), "Groceries", FlowType.EXPENSE);

      assertThat(categoryRepository.findByIdAndUser(category.getId(), insertUser())).isEmpty();
    }
  }

  @Nested
  @DisplayName("Find detail by ID and user")
  class FindDetailByIdAndUser {
    @Test
    @DisplayName("Should return audit timestamps")
    void shouldReturnAuditTimestamps() {
      UUID userId = insertUser();
      Category category = newCategory(userId, "Groceries", FlowType.EXPENSE);

      CategoryDetail detail = categoryRepository.findDetailByIdAndUser(category.getId(), userId).orElseThrow();
      SoftAssertions.assertSoftly(softly -> {
        softly.assertThat(detail.createdAt()).as("createdAt").isNotNull();
        softly.assertThat(detail.createdAt()).as("updatedAt").isNotNull();
      });
    }

    @Test
    @DisplayName("Should not return detail of another user")
    void shouldNotReturnDetailOfAnotherUser() {
      Category category = newCategory(insertUser(), "Groceries", FlowType.EXPENSE);

      assertThat(categoryRepository.findDetailByIdAndUser(category.getId(), insertUser())).isEmpty();
    }

    @Test
    @DisplayName("Should not return deleted category")
    void shouldNotReturnDeletedCategory() {
      UUID userId = insertUser();
      Category deleted = deletedCategory(userId, "Groceries", FlowType.EXPENSE);

      assertThat(categoryRepository.findDetailByIdAndUser(deleted.getId(), userId)).isEmpty();
    }
  }

  @Nested
  @DisplayName("Find by IDs and user")
  class FindByIdsAndUser {
    @Test
    @DisplayName("Should return only requested categories of the user")
    void shouldReturnOnlyRequestedCategoriesOfTheUser() {
      UUID userId = insertUser();
      Category a = newCategory(userId, "Category A", FlowType.EXPENSE);
      Category b = newCategory(userId, "Category B", FlowType.EXPENSE);
      newCategory(userId, "Category C", FlowType.EXPENSE);
      Category foreign = newCategory(insertUser(), "Category D", FlowType.EXPENSE);

      List<Category> result = categoryRepository.findByIdsAndUser(Set.of(a.getId(), b.getId(), foreign.getId()),
        userId);

      assertThat(result).extracting(Category::getId).containsExactlyInAnyOrder(a.getId(), b.getId());
    }

    @Test
    @DisplayName("Should include deleted categories")
    void shouldIncludeDeletedCategories() {
      UUID userId = insertUser();
      Category deleted = deletedCategory(userId, "Old", FlowType.EXPENSE);

      assertThat(categoryRepository.findByIdsAndUser(Set.of(deleted.getId()), userId)).extracting(Category::getId)
        .containsExactly(deleted.getId());
    }

    @Test
    @DisplayName("Should ignore unknown IDs")
    void shouldIgnoreUnknownIds() {
      assertThat(categoryRepository.findByIdsAndUser(Set.of(UUID.randomUUID()), insertUser())).isEmpty();
    }

    @Test
    @DisplayName("Should return empty for empty ID set")
    void shouldReturnEmptyForEmptyIdSet() {
      assertThat(categoryRepository.findByIdsAndUser(Set.of(), insertUser())).isEmpty();
    }
  }

  @Nested
  @DisplayName("Exists by name, flow type and user")
  class ExistsByNameAndFlowTypeAndUser {
    @Test
    @DisplayName("Should exist when active category matched")
    void shouldExistWhenActiveCategoryMatched() {
      UUID userId = insertUser();
      newCategory(userId, "Groceries", FlowType.EXPENSE);

      assertThat(categoryRepository.existsByNameAndFlowTypeAndUserId("Groceries", FlowType.EXPENSE, userId)).isTrue();
    }

    @Test
    @DisplayName("Should not exist when only deleted category matched")
    void shouldNotExistWhenOnlyDeletedCategoryMatched() {
      UUID userId = insertUser();
      deletedCategory(userId, "Groceries", FlowType.EXPENSE);

      assertThat(categoryRepository.existsByNameAndFlowTypeAndUserId("Groceries", FlowType.EXPENSE, userId)).isFalse();
    }

    @Test
    @DisplayName("Should not exist for different flow type or user")
    void shouldNotExistForDifferentFlowTypeOrUser() {
      UUID userId = insertUser();
      newCategory(userId, "Groceries", FlowType.EXPENSE);

      SoftAssertions.assertSoftly(softly -> {
        softly.assertThat(categoryRepository.existsByNameAndFlowTypeAndUserId("Groceries", FlowType.INCOME, userId))
          .as("other flow type").isFalse();
        softly
          .assertThat(categoryRepository.existsByNameAndFlowTypeAndUserId("Groceries", FlowType.EXPENSE, insertUser()))
          .as("other user").isFalse();
      });
    }
  }

  private Category newCategory(UUID userId, String name, FlowType flowType) {
    return categoryRepository.save(Category.create(userId, name, flowType));
  }

  private Category deletedCategory(UUID userId, String name, FlowType flowType) {
    Category category = newCategory(userId, name, flowType);
    category.delete();
    return categoryRepository.save(category);
  }
}
