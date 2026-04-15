package com.docutrack.config;

import com.docutrack.entity.CategoryEntity;
import com.docutrack.repository.CategoryRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@SuppressWarnings("null")
public class DataSeeder implements ApplicationRunner {

  private final CategoryRepository categoryRepository;

  @Override
  public void run(ApplicationArguments args) {
    seedCategories();
  }

  private void seedCategories() {
    List<String> names = List.of("Warranty", "Insurance", "Bills", "Vehicle");
    for (String name : names) {
      categoryRepository.findByNameIgnoreCase(name)
          .orElseGet(() -> categoryRepository.save(CategoryEntity.builder().name(name).build()));
    }
  }
}

