package com.docutrack.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class AppPlatformConverter implements AttributeConverter<AppPlatform, String> {

  @Override
  public String convertToDatabaseColumn(AppPlatform attribute) {
    return attribute == null ? null : attribute.getValue();
  }

  @Override
  public AppPlatform convertToEntityAttribute(String dbData) {
    return dbData == null ? null : AppPlatform.fromValue(dbData);
  }
}
