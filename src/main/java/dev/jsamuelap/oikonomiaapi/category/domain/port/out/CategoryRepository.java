package dev.jsamuelap.oikonomiaapi.category.domain.port.out;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import dev.jsamuelap.oikonomiaapi.category.domain.model.Category;
import dev.jsamuelap.oikonomiaapi.category.domain.model.FlowType;
import dev.jsamuelap.oikonomiaapi.category.domain.port.in.CategoryDetail;

public interface CategoryRepository {
  Optional<Category> findByIdAndUser(UUID id, UUID userId);

  Optional<CategoryDetail> findDetailByIdAndUser(UUID id, UUID userId);

  List<Category> findByUser(UUID userId);

  List<Category> findAllById(Set<UUID> categoryIds);

  Category save(Category category);

  boolean existsByNameAndFlowTypeAndUserId(String name, FlowType flowType, UUID userId);
}
