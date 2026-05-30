package com.docutrack.dto.admin;

import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AdminUserPageDto {
  List<AdminUserSummaryDto> content;
  int page;
  int size;
  long totalElements;
  int totalPages;
  boolean first;
  boolean last;
}
