package dev.jsamuelap.oikonomiaapi.user.infrastructure.out.persistence.jpa;

import java.time.Instant;
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
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "users")
@NoArgsConstructor
public class UserJpaEntity implements Persistable<UUID> {
  @Id
  @Column(nullable = false)
  private UUID id;

  @Size(max = 100)
  @NotNull
  @Column(name = "first_name", nullable = false, length = 100)
  private String firstName;

  @Size(max = 150)
  @NotNull
  @Column(name = "last_name", nullable = false, length = 150)
  private String lastName;

  @Size(max = 320)
  @NotNull
  @Column(nullable = false, unique = true, length = 320)
  private String email;

  @Size(max = 255)
  @NotNull
  @Column(name = "password_hash", nullable = false)
  private String passwordHash;

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
