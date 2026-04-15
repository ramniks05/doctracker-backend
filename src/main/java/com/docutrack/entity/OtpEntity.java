package com.docutrack.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "otps")
public class OtpEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 20)
  private String mobileNumber;

  @Column(nullable = false, length = 10)
  private String otp;

  @Column(nullable = false)
  private LocalDateTime expiryTime;

  @Column(nullable = false)
  private Boolean isUsed;

  @Column(nullable = false)
  private Integer attemptCount;

  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  private Instant createdAt;
}

