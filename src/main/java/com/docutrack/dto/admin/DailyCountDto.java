package com.docutrack.dto.admin;

import java.time.LocalDate;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class DailyCountDto {
  LocalDate date;
  long count;
}
