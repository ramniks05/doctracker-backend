package com.docutrack.repository;

import com.docutrack.entity.DocumentEntity;
import com.docutrack.entity.DocumentStatus;
import com.docutrack.repository.projection.DailyCountProjection;
import com.docutrack.repository.projection.KeyCountProjection;
import com.docutrack.repository.projection.UserDocumentCountProjection;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DocumentRepository extends JpaRepository<DocumentEntity, Long>, JpaSpecificationExecutor<DocumentEntity> {

  long countByStatus(DocumentStatus status);

  long countByImageUrlIsNotNull();

  @Query("SELECT COUNT(d) FROM DocumentEntity d WHERE d.imageUrl2 IS NOT NULL AND TRIM(d.imageUrl2) <> ''")
  long countWithSecondImage();

  @Query("SELECT COUNT(d) FROM DocumentEntity d WHERE d.ocrRawText IS NOT NULL AND TRIM(d.ocrRawText) <> ''")
  long countWithOcr();

  long countByWarrantyMonthsIsNotNull();

  long countByPurchaseDateIsNotNull();

  long countByExpiryDateIsNotNull();

  @Query("SELECT COUNT(d) FROM DocumentEntity d WHERE d.brandName IS NOT NULL AND TRIM(d.brandName) <> ''")
  long countWithBrandName();

  @Query("SELECT COUNT(d) FROM DocumentEntity d WHERE d.notes IS NOT NULL AND TRIM(d.notes) <> ''")
  long countWithNotes();

  @Query("""
      SELECT COUNT(d) FROM DocumentEntity d
      WHERE d.expiryDate IS NOT NULL
        AND d.expiryDate > :today
        AND d.expiryDate <= :until
      """)
  long countExpiringBetween(@Param("today") LocalDate today, @Param("until") LocalDate until);

  @Query("""
      SELECT COUNT(d) FROM DocumentEntity d
      WHERE d.status = com.docutrack.entity.DocumentStatus.EXPIRED
        AND d.expiryDate IS NOT NULL
        AND d.expiryDate >= :since
      """)
  long countExpiredSince(@Param("since") LocalDate since);

  long countByCreatedAtAfter(Instant after);

  @Query("""
      SELECT c.name AS key, COUNT(d) AS count
      FROM DocumentEntity d JOIN d.category c
      GROUP BY c.name
      ORDER BY COUNT(d) DESC
      """)
  List<KeyCountProjection> countGroupByCategoryName();

  @Query("SELECT d.status, COUNT(d) FROM DocumentEntity d GROUP BY d.status")
  List<Object[]> countGroupByStatus();

  @Query(
      value = """
          SELECT CAST(created_at AS date) AS day, COUNT(*)::bigint AS count
          FROM documents
          WHERE created_at >= :since
          GROUP BY CAST(created_at AS date)
          ORDER BY day
          """,
      nativeQuery = true)
  List<DailyCountProjection> countCreatedPerDaySince(@Param("since") Instant since);

  @Query("""
      SELECT d.user.id AS userId,
             COUNT(d) AS total,
             SUM(CASE WHEN d.status = com.docutrack.entity.DocumentStatus.ACTIVE THEN 1 ELSE 0 END) AS active,
             SUM(CASE WHEN d.status = com.docutrack.entity.DocumentStatus.EXPIRED THEN 1 ELSE 0 END) AS expired,
             SUM(CASE WHEN d.status = com.docutrack.entity.DocumentStatus.EXPIRING_SOON THEN 1 ELSE 0 END) AS expiringSoon
      FROM DocumentEntity d
      WHERE d.user.id IN :userIds
      GROUP BY d.user.id
      """)
  List<UserDocumentCountProjection> countByUserIds(@Param("userIds") Collection<Long> userIds);

  @Query("""
      SELECT c.name AS key, COUNT(d) AS count
      FROM DocumentEntity d JOIN d.category c
      WHERE d.user.id = :userId
      GROUP BY c.name
      ORDER BY COUNT(d) DESC
      """)
  List<KeyCountProjection> countByCategoryForUser(@Param("userId") Long userId);

  long countByUser_Id(Long userId);

  long countByUser_IdAndStatus(Long userId, DocumentStatus status);

  @Query("SELECT COUNT(d) FROM DocumentEntity d WHERE d.user.id = :userId AND d.imageUrl IS NOT NULL")
  long countWithImageForUser(@Param("userId") Long userId);

  @Query("""
      SELECT COUNT(d) FROM DocumentEntity d
      WHERE d.user.id = :userId AND d.ocrRawText IS NOT NULL AND TRIM(d.ocrRawText) <> ''
      """)
  long countWithOcrForUser(@Param("userId") Long userId);

  @org.springframework.data.jpa.repository.EntityGraph(attributePaths = "category")
  List<DocumentEntity> findTop10ByUser_IdOrderByCreatedAtDesc(Long userId);

  Page<DocumentEntity> findAllByUser_Id(Long userId, Pageable pageable);

  Page<DocumentEntity> findAllByUser_IdAndCategory_Id(Long userId, Long categoryId, Pageable pageable);

  Page<DocumentEntity> findAllByUser_IdAndStatus(Long userId, DocumentStatus status, Pageable pageable);

  Page<DocumentEntity> findAllByUser_IdAndCategory_IdAndStatus(
      Long userId, Long categoryId, DocumentStatus status, Pageable pageable);

  List<DocumentEntity> findAllByExpiryDate(LocalDate expiryDate);

  @Query("""
      SELECT CASE WHEN COUNT(d) > 0 THEN true ELSE false END
      FROM DocumentEntity d
      WHERE d.id <> :excludeId AND (d.imageUrl = :path OR d.imageUrl2 = :path)
      """)
  boolean existsOtherDocumentReferencing(@Param("excludeId") Long excludeId, @Param("path") String path);
}

