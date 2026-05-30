package com.docutrack.dto.admin;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CountByKeyDto {
  String key;
  long count;
}
