package dev.jsamuelap.oikonomiaapi.transaction.infrastructure.out.persistence.jpa;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.domain.Persistable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "transactions")
public class TransactionJpaEntity implements Persistable<UUID> {
  @Id
  @Column(nullable = false)
  private UUID id;

  @NotNull
  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @NotNull
  @Column(name = "category_id", nullable = false)
  private UUID categoryId;

  @NotNull
  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal amount;

  @NotNull
  @Column(name = "transaction_date", nullable = false)
  private LocalDate transactionDate;

  @Size(max = 255)
  private String notes;

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
