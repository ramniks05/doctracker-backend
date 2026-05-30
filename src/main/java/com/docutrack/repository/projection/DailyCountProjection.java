package com.docutrack.repository.projection;

import java.time.LocalDate;

public interface DailyCountProjection {
  LocalDate getDay();
  Long getCount();
}
