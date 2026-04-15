package com.docutrack.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "users")
public class UserEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /**
   * Mobile number used for OTP login (unique).
   */
  @Column(name = "mobile_number", nullable = false, unique = true, length = 20)
  private String mobileNumber;

  @Column(length = 120)
  private String name;

  @Column(length = 200)
  private String email;

  /**
   * Kept nullable in DB to allow Hibernate ddl-auto=update to evolve older schemas safely.
   * Service layer treats null as false.
   */
  @Builder.Default
  @Column
  private Boolean isVerified = Boolean.FALSE;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private Instant createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private Instant updatedAt;

  @PrePersist
  public void prePersist() {
    if (isVerified == null) {
      isVerified = Boolean.FALSE;
    }
  }

  @PreUpdate
  public void preUpdate() {
    if (isVerified == null) {
      isVerified = Boolean.FALSE;
    }
  }
}

