package dev.jsamuelap.oikonomiaapi.category.domain.port.in;

import java.util.UUID;

public interface DeleteCategoryUseCase {
  void deleteById(UUID id, UUID userId);
}
