package dev.jsamuelap.oikonomiaapi.category.infrastructure.out.persistence.jpa;

import org.mapstruct.Mapper;

import dev.jsamuelap.oikonomiaapi.category.domain.model.Category;

@Mapper(componentModel = "spring")
public interface CategoryPersistenceMapper {
  default Category toDomain(CategoryJpaEntity entity) {
    if (entity == null) {
      return null;
    }

    return Category.reconstitute(entity.getId(), entity.getUserId(), entity.getName(), entity.getFlowType());
  }

  default CategoryJpaEntity toEntity(Category category) {
    if (category == null) {
      return null;
    }

    CategoryJpaEntity entity = new CategoryJpaEntity();
    entity.setId(category.getId());
    entity.setUserId(category.getUserId());
    entity.setName(category.getName());
    entity.setFlowType(category.getFlowType());
    return entity;
  }
}
