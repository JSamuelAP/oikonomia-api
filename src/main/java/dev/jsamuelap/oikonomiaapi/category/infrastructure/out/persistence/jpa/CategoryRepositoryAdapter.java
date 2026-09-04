package dev.jsamuelap.oikonomiaapi.category.infrastructure.out.persistence.jpa;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Component;

import dev.jsamuelap.oikonomiaapi.category.domain.model.Category;
import dev.jsamuelap.oikonomiaapi.category.domain.model.FlowType;
import dev.jsamuelap.oikonomiaapi.category.domain.port.in.CategoryDetail;
import dev.jsamuelap.oikonomiaapi.category.domain.port.out.CategoryRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CategoryRepositoryAdapter implements CategoryRepository {
  private final CategoryJpaRepository jpaRepository;
  private final CategoryPersistenceMapper mapper;

  @Override
  public List<Category> findByUser(UUID userId) {
    return jpaRepository.findByUserIdAndDeletedAtIsNull(userId).stream().map(mapper::toDomain).toList();
  }

  @Override
  public Optional<CategoryDetail> findDetailByIdAndUser(UUID id, UUID userId) {
    return jpaRepository.findByIdAndUserIdAndDeletedAtIsNull(id, userId).map(mapper::toDetail);
  }

  @Override
  public List<Category> findAllById(Set<UUID> categoryIds) {
    return jpaRepository.findAllById(categoryIds).stream().map(mapper::toDomain).toList();
  }

  @Override
  public Category save(Category category) {
    CategoryJpaEntity entity = jpaRepository.findById(category.getId())
      .map(existing -> updateEntity(existing, category)).orElseGet(() -> mapper.toEntity(category));
    CategoryJpaEntity saved = jpaRepository.save(entity);
    return mapper.toDomain(saved);
  }

  @Override
  public boolean existsByNameAndFlowTypeAndUserId(String name, FlowType flowType, UUID userId) {
    return jpaRepository.existsByUserIdAndNameAndFlowTypeAndDeletedAtIsNull(userId, name, flowType);
  }

  @Override
  public Optional<Category> findByIdAndUser(UUID id, UUID userId) {
    return jpaRepository.findByIdAndUserId(id, userId).map(mapper::toDomain);
  }

  private CategoryJpaEntity updateEntity(CategoryJpaEntity entity, Category category) {
    entity.setName(category.getName());
    entity.setFlowType(category.getFlowType());
    entity.setDeletedAt(category.getDeletedAt());
    return entity;
  }
}
