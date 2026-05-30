package com.docutrack.repository;

import com.docutrack.entity.NotificationChannel;
import com.docutrack.entity.NotificationLogEntity;
import com.docutrack.entity.ReminderType;
import com.docutrack.repository.projection.DailyCountProjection;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationLogRepository extends JpaRepository<NotificationLogEntity, Long> {
  boolean existsByDocumentIdAndChannelAndReminderType(Long documentId, NotificationChannel channel, ReminderType reminderType);

  @Query("SELECT n.reminderType, COUNT(n) FROM NotificationLogEntity n GROUP BY n.reminderType")
  List<Object[]> countGroupByReminderType();

  @Query("SELECT n.channel, COUNT(n) FROM NotificationLogEntity n GROUP BY n.channel")
  List<Object[]> countGroupByChannel();

  @Query(
      value = """
          SELECT CAST(created_at AS date) AS day, COUNT(*)::bigint AS count
          FROM notification_logs
          WHERE created_at >= :since
          GROUP BY CAST(created_at AS date)
          ORDER BY day
          """,
      nativeQuery = true)
  List<DailyCountProjection> countPerDaySince(@Param("since") Instant since);
}
