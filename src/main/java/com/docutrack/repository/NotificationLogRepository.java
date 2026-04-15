package com.docutrack.repository;

import com.docutrack.entity.NotificationChannel;
import com.docutrack.entity.NotificationLogEntity;
import com.docutrack.entity.ReminderType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationLogRepository extends JpaRepository<NotificationLogEntity, Long> {
  boolean existsByDocumentIdAndChannelAndReminderType(Long documentId, NotificationChannel channel, ReminderType reminderType);
}
