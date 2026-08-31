package dev.jsamuelap.oikonomiaapi.category.infrastructure.out.persistence.jpa;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;
import org.springframework.data.domain.Persistable;

import dev.jsamuelap.oikonomiaapi.category.domain.model.FlowType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "categories")
public class CategoryJpaEntity implements Persistable<UUID> {
  @Id
  @Column(nullable = false)
  private UUID id;

  @NotNull
  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Size(max = 50)
  @NotNull
  @Column(nullable = false, length = 50)
  private String name;

  @NotNull
  @Column(name = "flow_type", nullable = false, columnDefinition = "transaction_type not null")
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  @Enumerated(EnumType.STRING)
  private FlowType flowType;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @Column(name = "deleted_at")
  private Instant deletedAt;

  @Transient
  private boolean isNewEntity = true;

  @Override
  public boolean isNew() {
    return isNewEntity;
  }

  @PostLoad
  @PostPersist
  void markNotNew() {
    this.isNewEntity = false;
  }
}
