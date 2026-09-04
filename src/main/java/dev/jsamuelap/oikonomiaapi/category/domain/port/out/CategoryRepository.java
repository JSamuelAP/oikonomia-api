package dev.jsamuelap.oikonomiaapi.category.domain.port.out;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import dev.jsamuelap.oikonomiaapi.category.domain.model.Category;
import dev.jsamuelap.oikonomiaapi.category.domain.port.in.CategoryDetail;

public interface CategoryRepository {
  List<Category> findByUser(UUID userId);

  Optional<CategoryDetail> findByIdAndUser(UUID id, UUID userId);

  List<Category> findAllById(Set<UUID> categoryIds);

  Category save(Category category);
}
