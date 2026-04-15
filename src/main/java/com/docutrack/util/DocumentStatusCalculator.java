package com.docutrack.util;

import com.docutrack.entity.DocumentStatus;
import java.time.Clock;
import java.time.LocalDate;
import org.springframework.stereotype.Component;

@Component
public class DocumentStatusCalculator {

  private final Clock clock;

  public DocumentStatusCalculator(Clock clock) {
    this.clock = clock;
  }

  public DocumentStatus calculate(LocalDate expiryDate) {
    if (expiryDate == null) {
      return DocumentStatus.ACTIVE;
    }
    LocalDate today = LocalDate.now(clock);
    if (expiryDate.isBefore(today)) {
      return DocumentStatus.EXPIRED;
    }
    if (!expiryDate.isAfter(today.plusDays(7))) {
      return DocumentStatus.EXPIRING_SOON;
    }
    return DocumentStatus.ACTIVE;
  }
}

