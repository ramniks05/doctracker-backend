package com.docutrack.repository;

import com.docutrack.entity.DocumentEntity;
import com.docutrack.entity.DocumentStatus;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface DocumentRepository extends JpaRepository<DocumentEntity, Long>, JpaSpecificationExecutor<DocumentEntity> {

  Page<DocumentEntity> findAllByUser_Id(Long userId, Pageable pageable);

  Page<DocumentEntity> findAllByUser_IdAndCategory_Id(Long userId, Long categoryId, Pageable pageable);

  Page<DocumentEntity> findAllByUser_IdAndStatus(Long userId, DocumentStatus status, Pageable pageable);

  Page<DocumentEntity> findAllByUser_IdAndCategory_IdAndStatus(
      Long userId, Long categoryId, DocumentStatus status, Pageable pageable);

  List<DocumentEntity> findAllByExpiryDate(LocalDate expiryDate);
}

