package com.docutrack.repository;

import com.docutrack.entity.OtpEntity;
import com.docutrack.repository.projection.DailyCountProjection;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OtpRepository extends JpaRepository<OtpEntity, Long> {
  Optional<OtpEntity> findTopByMobileNumberOrderByIdDesc(String mobileNumber);

  long countByMobileNumberAndCreatedAtAfter(String mobileNumber, Instant after);

  long countByCreatedAtAfter(Instant after);

  @Query(
      value = """
          SELECT CAST(created_at AS date) AS day, COUNT(*)::bigint AS count
          FROM otps
          WHERE created_at >= :since
          GROUP BY CAST(created_at AS date)
          ORDER BY day
          """,
      nativeQuery = true)
  List<DailyCountProjection> countPerDaySince(@Param("since") Instant since);
}

