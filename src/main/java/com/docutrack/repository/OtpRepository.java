package com.docutrack.repository;

import com.docutrack.entity.OtpEntity;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OtpRepository extends JpaRepository<OtpEntity, Long> {
  Optional<OtpEntity> findTopByMobileNumberOrderByIdDesc(String mobileNumber);

  long countByMobileNumberAndCreatedAtAfter(String mobileNumber, Instant after);
}

