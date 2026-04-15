package com.docutrack.service.impl;

import com.docutrack.dto.document.DocumentCreateMultipartRequestDto;
import com.docutrack.dto.document.DocumentCreateRequestDto;
import com.docutrack.dto.document.DocumentResponseDto;
import com.docutrack.dto.document.DocumentUpdateRequestDto;
import com.docutrack.entity.CategoryEntity;
import com.docutrack.entity.DocumentEntity;
import com.docutrack.entity.DocumentStatus;
import com.docutrack.entity.UserEntity;
import com.docutrack.exception.BadRequestException;
import com.docutrack.exception.NotFoundException;
import com.docutrack.repository.CategoryRepository;
import com.docutrack.repository.DocumentRepository;
import com.docutrack.repository.UserRepository;
import com.docutrack.service.DocumentService;
import com.docutrack.service.FileStorageService;
import com.docutrack.util.DocumentStatusCalculator;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class DocumentServiceImpl implements DocumentService {

  private final DocumentRepository documentRepository;
  private final UserRepository userRepository;
  private final CategoryRepository categoryRepository;
  private final DocumentStatusCalculator statusCalculator;
  private final FileStorageService fileStorageService;

  @Override
  @Transactional
  public DocumentResponseDto create(Long userId, DocumentCreateRequestDto request) {
    UserEntity user = userRepository.findById(userId)
        .orElseThrow(() -> new NotFoundException("User not found: " + userId));
    CategoryEntity category = categoryRepository.findById(request.getCategoryId())
        .orElseThrow(() -> new NotFoundException("Category not found: " + request.getCategoryId()));

    DocumentEntity entity = DocumentEntity.builder()
        .user(user)
        .category(category)
        .name(request.getName())
        .brandName(request.getBrandName())
        .purchaseDate(request.getPurchaseDate())
        .expiryDate(request.getExpiryDate())
        .notes(request.getNotes())
        .imageUrl(request.getImageUrl())
        .status(statusCalculator.calculate(request.getExpiryDate()))
        .build();

    DocumentEntity saved = documentRepository.save(entity);
    return toDto(saved);
  }

  @Override
  @Transactional
  public DocumentResponseDto createWithImage(Long userId, DocumentCreateMultipartRequestDto request) {
    UserEntity user = userRepository.findById(userId)
        .orElseThrow(() -> new NotFoundException("User not found: " + userId));
    CategoryEntity category = categoryRepository.findById(request.getCategoryId())
        .orElseThrow(() -> new NotFoundException("Category not found: " + request.getCategoryId()));

    LocalDate purchaseDate = request.getPurchaseDate();
    if (purchaseDate == null) {
      throw new BadRequestException("purchaseDate is required");
    }

    Integer warrantyMonths = request.getWarrantyMonths();
    if (warrantyMonths == null) {
      throw new BadRequestException("warrantyMonths is required");
    }

    LocalDate expiryDate = request.getExpiryDate();
    if (expiryDate == null) {
      expiryDate = purchaseDate.plusMonths(warrantyMonths);
    }

    String imageUrl = fileStorageService.store(request.getFile());

    DocumentEntity entity = DocumentEntity.builder()
        .user(user)
        .category(category)
        .name(request.getName())
        .brandName(request.getBrandName())
        .purchaseDate(purchaseDate)
        .warrantyMonths(warrantyMonths)
        .expiryDate(expiryDate)
        .notes(request.getNotes())
        .ocrRawText(request.getOcrRawText())
        .imageUrl(imageUrl)
        .status(statusCalculator.calculate(expiryDate))
        .build();

    return toDto(documentRepository.save(entity));
  }

  @Override
  @Transactional(readOnly = true)
  public Page<DocumentResponseDto> getAll(Long userId, Long categoryId, DocumentStatus status, Pageable pageable) {
    Page<DocumentEntity> page;
    if (categoryId != null && status != null) {
      page = documentRepository.findAllByUser_IdAndCategory_IdAndStatus(userId, categoryId, status, pageable);
    } else if (categoryId != null) {
      page = documentRepository.findAllByUser_IdAndCategory_Id(userId, categoryId, pageable);
    } else if (status != null) {
      page = documentRepository.findAllByUser_IdAndStatus(userId, status, pageable);
    } else {
      page = documentRepository.findAllByUser_Id(userId, pageable);
    }
    return page.map(this::toDto);
  }

  @Override
  @Transactional(readOnly = true)
  public DocumentResponseDto getById(Long id, Long userId) {
    DocumentEntity entity = documentRepository.findById(id)
        .orElseThrow(() -> new NotFoundException("Document not found: " + id));
    if (!entity.getUser().getId().equals(userId)) {
      throw new NotFoundException("Document not found: " + id);
    }
    return toDto(entity);
  }

  @Override
  @Transactional
  public DocumentResponseDto update(Long id, Long userId, DocumentUpdateRequestDto request) {
    DocumentEntity entity = documentRepository.findById(id)
        .orElseThrow(() -> new NotFoundException("Document not found: " + id));

    if (!entity.getUser().getId().equals(userId)) {
      throw new NotFoundException("Document not found: " + id);
    }

    CategoryEntity category = categoryRepository.findById(request.getCategoryId())
        .orElseThrow(() -> new NotFoundException("Category not found: " + request.getCategoryId()));

    entity.setCategory(category);
    entity.setName(request.getName());
    entity.setBrandName(request.getBrandName());
    entity.setPurchaseDate(request.getPurchaseDate());
    entity.setExpiryDate(request.getExpiryDate());
    entity.setNotes(request.getNotes());
    entity.setImageUrl(request.getImageUrl());
    entity.setStatus(statusCalculator.calculate(request.getExpiryDate()));

    return toDto(documentRepository.save(entity));
  }

  @Override
  @Transactional
  public void delete(Long id, Long userId) {
    DocumentEntity entity = documentRepository.findById(id)
        .orElseThrow(() -> new NotFoundException("Document not found: " + id));
    if (!entity.getUser().getId().equals(userId)) {
      throw new NotFoundException("Document not found: " + id);
    }
    documentRepository.delete(entity);
  }

  private DocumentResponseDto toDto(DocumentEntity d) {
    return DocumentResponseDto.builder()
        .id(d.getId())
        .userId(d.getUser().getId())
        .name(d.getName())
        .categoryId(d.getCategory().getId())
        .categoryName(d.getCategory().getName())
        .brandName(d.getBrandName())
        .purchaseDate(d.getPurchaseDate())
        .warrantyMonths(d.getWarrantyMonths())
        .expiryDate(d.getExpiryDate())
        .notes(d.getNotes())
        .ocrRawText(d.getOcrRawText())
        .imageUrl(d.getImageUrl())
        .status(d.getStatus())
        .createdAt(d.getCreatedAt())
        .updatedAt(d.getUpdatedAt())
        .build();
  }
}

