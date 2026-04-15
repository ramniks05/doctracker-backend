package com.docutrack.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
    name = "notification_logs",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_notification_logs_doc_channel_type",
            columnNames = {"document_id", "channel", "reminder_type"}
        )
    }
)
public class NotificationLogEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "document_id", nullable = false)
  private Long documentId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private NotificationChannel channel;

  @Enumerated(EnumType.STRING)
  @Column(name = "reminder_type", nullable = false, length = 30)
  private ReminderType reminderType;

  @Column(name = "recipient_mobile", nullable = false, length = 20)
  private String recipientMobile;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;
}
