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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
  @Value("${app.public-base-url:}")
  private String publicBaseUrl;

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
        .imageUrl2(request.getImageUrl2())
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

    List<org.springframework.web.multipart.MultipartFile> uploadFiles = collectUploadFiles(request);
    if (uploadFiles.isEmpty()) {
      throw new BadRequestException("At least one file is required");
    }
    if (uploadFiles.size() > 2) {
      throw new BadRequestException("Maximum 2 files are allowed");
    }
    String imageUrl = fileStorageService.store(uploadFiles.get(0));
    String imageUrl2 = uploadFiles.size() > 1 ? fileStorageService.store(uploadFiles.get(1)) : null;

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
        .imageUrl2(imageUrl2)
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

    String previousImageUrl = entity.getImageUrl();
    String previousImageUrl2 = entity.getImageUrl2();

    entity.setCategory(category);
    entity.setName(request.getName());
    entity.setBrandName(request.getBrandName());
    entity.setPurchaseDate(request.getPurchaseDate());
    entity.setExpiryDate(request.getExpiryDate());
    entity.setNotes(request.getNotes());
    entity.setImageUrl(request.getImageUrl());
    entity.setImageUrl2(request.getImageUrl2());
    entity.setStatus(statusCalculator.calculate(request.getExpiryDate()));

    DocumentEntity saved = documentRepository.save(entity);
    deleteReplacedImageIfOrphan(previousImageUrl, request.getImageUrl(), saved.getId());
    deleteReplacedImageIfOrphan(previousImageUrl2, request.getImageUrl2(), saved.getId());
    return toDto(saved);
  }

  @Override
  @Transactional
  public void delete(Long id, Long userId) {
    DocumentEntity entity = documentRepository.findById(id)
        .orElseThrow(() -> new NotFoundException("Document not found: " + id));
    if (!entity.getUser().getId().equals(userId)) {
      throw new NotFoundException("Document not found: " + id);
    }
    deleteDocumentImagesIfOrphan(entity);
    documentRepository.delete(entity);
  }

  private void deleteDocumentImagesIfOrphan(DocumentEntity entity) {
    deleteStoredImageIfOrphan(entity.getImageUrl(), entity.getId());
    deleteStoredImageIfOrphan(entity.getImageUrl2(), entity.getId());
  }

  private void deleteReplacedImageIfOrphan(String previousPath, String newPath, Long documentId) {
    if (!storedPathChanged(previousPath, newPath)) {
      return;
    }
    deleteStoredImageIfOrphan(previousPath, documentId);
  }

  private void deleteStoredImageIfOrphan(String path, Long excludeDocumentId) {
    String normalized = fileStorageService.normalizeStoredPath(path);
    if (normalized == null) {
      return;
    }
    if (documentRepository.existsOtherDocumentReferencing(excludeDocumentId, normalized)) {
      return;
    }
    fileStorageService.deleteLocalFile(normalized);
  }

  private boolean storedPathChanged(String previousPath, String newPath) {
    return !Objects.equals(
        fileStorageService.normalizeStoredPath(previousPath),
        fileStorageService.normalizeStoredPath(newPath));
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
        .imageUrl(toPublicUrl(d.getImageUrl()))
        .imageUrl2(toPublicUrl(d.getImageUrl2()))
        .imageUrls(imageUrls(d))
        .status(d.getStatus())
        .createdAt(d.getCreatedAt())
        .updatedAt(d.getUpdatedAt())
        .build();
  }

  private List<String> imageUrls(DocumentEntity d) {
    List<String> urls = new ArrayList<>(2);
    if (d.getImageUrl() != null && !d.getImageUrl().isBlank()) {
      urls.add(toPublicUrl(d.getImageUrl()));
    }
    if (d.getImageUrl2() != null && !d.getImageUrl2().isBlank()) {
      urls.add(toPublicUrl(d.getImageUrl2()));
    }
    return urls;
  }

  private List<org.springframework.web.multipart.MultipartFile> collectUploadFiles(DocumentCreateMultipartRequestDto request) {
    List<org.springframework.web.multipart.MultipartFile> files = new ArrayList<>(2);
    if (request.getFiles() != null) {
      for (org.springframework.web.multipart.MultipartFile f : request.getFiles()) {
        if (f != null && !f.isEmpty()) {
          files.add(f);
        }
      }
    }
    if (files.isEmpty() && request.getFile() != null && !request.getFile().isEmpty()) {
      files.add(request.getFile());
    }
    return files;
  }

  private String toPublicUrl(String imageUrl) {
    if (imageUrl == null || imageUrl.isBlank()) return imageUrl;
    if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) return imageUrl;
    if (publicBaseUrl == null || publicBaseUrl.isBlank()) return imageUrl;

    String base = publicBaseUrl.endsWith("/") ? publicBaseUrl.substring(0, publicBaseUrl.length() - 1) : publicBaseUrl;
    String path = imageUrl.startsWith("/") ? imageUrl : ("/" + imageUrl);
    return base + path;
  }
}

