package com.docutrack.service.impl;

import com.docutrack.entity.DocumentEntity;
import com.docutrack.entity.NotificationChannel;
import com.docutrack.entity.NotificationLogEntity;
import com.docutrack.entity.ReminderType;
import com.docutrack.repository.DocumentRepository;
import com.docutrack.repository.NotificationLogRepository;
import com.docutrack.service.WhatsAppNotificationService;
import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class ExpiryReminderSchedulerService {

  private static final Logger log = LoggerFactory.getLogger(ExpiryReminderSchedulerService.class);

  private final DocumentRepository documentRepository;
  private final NotificationLogRepository notificationLogRepository;
  private final WhatsAppNotificationService whatsAppNotificationService;
  private final Clock clock;

  @Scheduled(cron = "${app.notifications.expiry-cron:0 0 9 * * *}")
  @Transactional
  public void sendExpiryReminders() {
    LocalDate today = LocalDate.now(clock);
    processReminder(today, 7, ReminderType.DAYS_7);
    processReminder(today, 1, ReminderType.DAYS_1);
    processReminder(today, 0, ReminderType.DAY_0);
  }

  private void processReminder(LocalDate today, int daysOffset, ReminderType reminderType) {
    LocalDate targetDate = today.plusDays(daysOffset);
    List<DocumentEntity> documents = documentRepository.findAllByExpiryDate(targetDate);

    if (documents.isEmpty()) {
      return;
    }

    for (DocumentEntity doc : documents) {
      Long documentId = doc.getId();
      if (documentId == null || doc.getUser() == null) {
        continue;
      }
      String mobile = doc.getUser().getMobileNumber();
      if (mobile == null || mobile.isBlank()) {
        continue;
      }

      boolean alreadySent = notificationLogRepository.existsByDocumentIdAndChannelAndReminderType(
          documentId, NotificationChannel.WHATSAPP, reminderType);
      if (alreadySent) {
        continue;
      }

      String message = buildMessage(doc, targetDate);
      boolean sent = whatsAppNotificationService.sendText(mobile, message);
      if (!sent) {
        continue;
      }

      notificationLogRepository.save(NotificationLogEntity.builder()
          .documentId(documentId)
          .channel(NotificationChannel.WHATSAPP)
          .reminderType(reminderType)
          .recipientMobile(mobile)
          .build());
      log.info("Reminder sent documentId={} type={} mobile={}", documentId, reminderType, mobile);
    }
  }

  private String buildMessage(DocumentEntity doc, LocalDate expiryDate) {
    LocalDate today = LocalDate.now(clock);
    long daysLeft = ChronoUnit.DAYS.between(today, expiryDate);

    String itemName = (doc.getName() == null || doc.getName().isBlank()) ? "your item" : doc.getName();
    if (daysLeft > 1) {
      return "Reminder: warranty for " + itemName + " will expire in " + daysLeft + " days on " + expiryDate + ".";
    }
    if (daysLeft == 1) {
      return "Reminder: warranty for " + itemName + " will expire tomorrow (" + expiryDate + ").";
    }
    return "Reminder: warranty for " + itemName + " expires today (" + expiryDate + ").";
  }
}
