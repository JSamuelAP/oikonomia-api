package dev.jsamuelap.oikonomiaapi.user.infrastructure.out.persistence.jpa;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
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
@Table(name = "refresh_tokens")
public class RefreshTokenJpaEntity implements Persistable<UUID> {
  @Id
  @Column(nullable = false)
  private UUID id;

  @NotNull
  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Size(max = 64)
  @NotNull
  @Column(name = "token_hash", nullable = false, unique = true, length = 64)
  private String tokenHash;

  @NotNull
  @Column(name = "expires_at", nullable = false)
  private Instant expiresAt;

  @NotNull
  @Column(nullable = false)
  private Boolean revoked;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "replaced_by")
  private UUID replacedBy;

  @Transient
  private boolean isNewEntity = true;

  @Override
  public boolean isNew() {
    return isNewEntity;
  }

  @PostLoad
  @PostPersist
  void markNotNew() {
    isNewEntity = false;
  }
}
