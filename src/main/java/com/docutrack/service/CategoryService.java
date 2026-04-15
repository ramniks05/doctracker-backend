package com.docutrack.service;

import com.docutrack.dto.category.CategoryDto;
import java.util.List;

public interface CategoryService {
  List<CategoryDto> getAll();
}

