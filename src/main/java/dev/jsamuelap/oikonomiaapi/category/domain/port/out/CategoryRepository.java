package dev.jsamuelap.oikonomiaapi.category.domain.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import dev.jsamuelap.oikonomiaapi.category.domain.model.Category;

public interface CategoryRepository {
  List<Category> findByUser(UUID userId);

  Optional<Category> findByIdAndUser(UUID id, UUID userId);

  Category save(Category category);
}
