package com.docutrack.service.impl;

import com.docutrack.dto.category.CategoryDto;
import com.docutrack.repository.CategoryRepository;
import com.docutrack.service.CategoryService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

  private final CategoryRepository categoryRepository;

  @Override
  public List<CategoryDto> getAll() {
    return categoryRepository.findAll().stream()
        .map(c -> CategoryDto.builder().id(c.getId()).name(c.getName()).build())
        .toList();
  }
}

