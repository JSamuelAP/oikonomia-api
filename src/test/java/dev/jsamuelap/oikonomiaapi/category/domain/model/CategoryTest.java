package dev.jsamuelap.oikonomiaapi.category.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.UUID;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import dev.jsamuelap.oikonomiaapi.shared.domain.exception.DomainException;

@DisplayName("Category domain model")
public class CategoryTest {
  private static final UUID VALID_USER_ID = UUID.randomUUID();
  private static final String VALID_NAME = "Groceries";
  private static final FlowType VALID_FLOW_TYPE = FlowType.EXPENSE;

  @Nested
  @DisplayName("Category creation")
  class Creation {
    @Test
    @DisplayName("Should create valid category with all fields")
    void shouldCreateValidCategory() {
      Category category = Category.create(VALID_USER_ID, VALID_NAME, VALID_FLOW_TYPE);

      SoftAssertions.assertSoftly(softly -> {
        softly.assertThat(category).as("category").isNotNull();
        softly.assertThat(category.getId()).as("id").isNotNull();
        softly.assertThat(category.getUserId()).as("userId").isEqualTo(VALID_USER_ID);
        softly.assertThat(category.getName()).as("name").isEqualTo(VALID_NAME);
        softly.assertThat(category.getFlowType()).as("flowType").isEqualTo(VALID_FLOW_TYPE);
        softly.assertThat(category.isDeleted()).as("isDeleted").isFalse();
      });
    }

    @Nested
    @DisplayName("Name validation")
    class Name {
      @ParameterizedTest(name = "should reject: {0}")
      @DisplayName("Should reject blank name")
      @NullAndEmptySource
      @ValueSource(strings = {" ", "   "})
      void shouldRejectBlankName(String invalidName) {
        assertThatThrownBy(() -> Category.create(VALID_USER_ID, invalidName, VALID_FLOW_TYPE))
          .isInstanceOf(DomainException.class).hasMessageContaining("no puede estar vacío");
      }

      @Test
      @DisplayName("Should reject name shorter than minimum length")
      void shouldRejectNameTooShort() {
        String tooShortName = "a".repeat(Category.MIN_NAME_LENGTH - 1);
        assertThatThrownBy(() -> Category.create(VALID_USER_ID, tooShortName, VALID_FLOW_TYPE))
          .isInstanceOf(DomainException.class)
          .hasMessageContaining("debe tener mínimo %s caracteres".formatted(Category.MIN_NAME_LENGTH));
      }

      @Test
      @DisplayName("Should reject name exceeding maximum length")
      void shouldRejectNameTooLong() {
        String tooLongName = "a".repeat(Category.MAX_NAME_LENGTH + 1);
        assertThatThrownBy(() -> Category.create(VALID_USER_ID, tooLongName, VALID_FLOW_TYPE))
          .isInstanceOf(DomainException.class)
          .hasMessageContaining("no puede exceder más de %s caracteres".formatted(Category.MAX_NAME_LENGTH));
      }

      @Test
      @DisplayName("Should accept name at minimum length")
      void shouldAcceptNameAtMinLength() {
        String exactlyMinName = "a".repeat(Category.MIN_NAME_LENGTH);
        Category category = Category.create(VALID_USER_ID, exactlyMinName, VALID_FLOW_TYPE);
        assertThat(category.getName()).hasSize(Category.MIN_NAME_LENGTH);
      }

      @Test
      @DisplayName("Should accept name at maximum length")
      void shouldAcceptNameAtMaxLength() {
        String exactlyMaxName = "a".repeat(Category.MAX_NAME_LENGTH);
        Category category = Category.create(VALID_USER_ID, exactlyMaxName, VALID_FLOW_TYPE);
        assertThat(category.getName()).hasSize(Category.MAX_NAME_LENGTH);
      }
    }

    @Nested
    @DisplayName("Flow type validation")
    class FlowTypeValidation {
      @Test
      @DisplayName("Should accept INCOME flow type")
      void shouldAcceptIncomeFlowType() {
        Category category = Category.create(VALID_USER_ID, VALID_NAME, FlowType.INCOME);
        assertThat(category.getFlowType()).isEqualTo(FlowType.INCOME);
      }

      @Test
      @DisplayName("Should accept EXPENSE flow type")
      void shouldAcceptExpenseFlowType() {
        Category category = Category.create(VALID_USER_ID, VALID_NAME, FlowType.EXPENSE);
        assertThat(category.getFlowType()).isEqualTo(FlowType.EXPENSE);
      }

      @Test
      @DisplayName("Should reject null flow type")
      void shouldRejectNullFlowType() {
        assertThatThrownBy(() -> Category.create(VALID_USER_ID, VALID_NAME, null)).isInstanceOf(DomainException.class)
          .hasMessageContaining("no puede ser nulo");
      }
    }

    @Test
    @DisplayName("Should reject null user id")
    void shouldRejectNullUserId() {
      assertThatThrownBy(() -> Category.create(null, VALID_NAME, VALID_FLOW_TYPE)).isInstanceOf(DomainException.class)
        .hasMessageContaining("debe estar asociado a un usuario");
    }
  }

  @Nested
  @DisplayName("Category reconstitution from persistence")
  class Reconstitution {
    @Test
    @DisplayName("Should reconstitute category without re-validating fields")
    void shouldReconstituteWithoutRevalidating() {
      Category category = Category.reconstitute(null, null, "", null, null);

      SoftAssertions.assertSoftly(softly -> {
        softly.assertThat(category).as("category").isNotNull();
        softly.assertThat(category.getId()).as("id").isNull();
        softly.assertThat(category.getUserId()).as("userId").isNull();
        softly.assertThat(category.getName()).as("name").isBlank();
        softly.assertThat(category.getFlowType()).as("flowType").isNull();
        softly.assertThat(category.isDeleted()).as("isDeleted").isFalse();
        softly.assertThat(category.getDeletedAt()).as("deletedAt").isNull();
      });
    }

    @Test
    @DisplayName("Should reconstitute category with all fields")
    void shouldReconstituteWithAllFields() {
      UUID id = UUID.randomUUID();
      UUID userId = UUID.randomUUID();
      Instant deletedAt = Instant.now();

      Category category = Category.reconstitute(id, userId, VALID_NAME, VALID_FLOW_TYPE, deletedAt);

      SoftAssertions.assertSoftly(softly -> {
        softly.assertThat(category).as("category").isNotNull();
        softly.assertThat(category.getId()).as("id").isEqualTo(id);
        softly.assertThat(category.getUserId()).as("userId").isEqualTo(userId);
        softly.assertThat(category.getName()).as("name").isEqualTo(VALID_NAME);
        softly.assertThat(category.getFlowType()).as("flowType").isEqualTo(VALID_FLOW_TYPE);
        softly.assertThat(category.isDeleted()).as("isDeleted").isTrue();
        softly.assertThat(category.getDeletedAt()).as("deletedAt").isEqualTo(deletedAt);
      });
    }
  }

  @Nested
  @DisplayName("Category deletion")
  class Deletion {
    @Test
    @DisplayName("Should mark category as deleted")
    void shouldDeleteCategory() {
      Category category = Category.create(VALID_USER_ID, VALID_NAME, VALID_FLOW_TYPE);
      category.delete();

      SoftAssertions.assertSoftly(softly -> {
        softly.assertThat(category.isDeleted()).as("isDeleted").isTrue();
        softly.assertThat(category.getDeletedAt()).as("deletedAt").isNotNull();
      });
    }

    @Test
    @DisplayName("Should reject deleting already deleted category")
    void shouldRejectDeletingAlreadyDeletedCategory() {
      Category category = Category.create(VALID_USER_ID, VALID_NAME, VALID_FLOW_TYPE);
      category.delete();

      assertThatThrownBy(category::delete).isInstanceOf(DomainException.class)
        .hasMessageContaining("ya está eliminada");
    }
  }
}
