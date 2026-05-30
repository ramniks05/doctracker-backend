package com.docutrack.repository;

import com.docutrack.entity.UserEntity;
import com.docutrack.repository.projection.DailyCountProjection;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
  Optional<UserEntity> findByMobileNumber(String mobileNumber);

  long countByIsVerifiedTrue();

  long countByIsVerifiedFalse();

  @Query("SELECT COUNT(u) FROM UserEntity u WHERE u.name IS NOT NULL AND TRIM(u.name) <> ''")
  long countWithName();

  @Query("SELECT COUNT(u) FROM UserEntity u WHERE u.email IS NOT NULL AND TRIM(u.email) <> ''")
  long countWithEmail();

  @Query("""
      SELECT COUNT(u) FROM UserEntity u
      WHERE u.name IS NOT NULL AND TRIM(u.name) <> ''
        AND u.email IS NOT NULL AND TRIM(u.email) <> ''
      """)
  long countWithCompleteProfile();

  long countByCreatedAtAfter(Instant after);

  @Query("""
      SELECT u FROM UserEntity u
      WHERE (:q IS NULL OR :q = '' OR
        LOWER(u.mobileNumber) LIKE LOWER(CONCAT('%', :q, '%')) OR
        LOWER(COALESCE(u.name, '')) LIKE LOWER(CONCAT('%', :q, '%')) OR
        LOWER(COALESCE(u.email, '')) LIKE LOWER(CONCAT('%', :q, '%')))
      """)
  Page<UserEntity> search(@Param("q") String query, Pageable pageable);

  @Query(
      value = """
          SELECT CAST(created_at AS date) AS day, COUNT(*)::bigint AS count
          FROM users
          WHERE created_at >= :since
          GROUP BY CAST(created_at AS date)
          ORDER BY day
          """,
      nativeQuery = true)
  java.util.List<DailyCountProjection> countSignupsPerDaySince(@Param("since") Instant since);
}

