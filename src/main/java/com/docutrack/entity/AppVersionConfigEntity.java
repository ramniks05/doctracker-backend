package com.docutrack.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "app_version_config")
public class AppVersionConfigEntity {

  @Id
  @Convert(converter = AppPlatformConverter.class)
  @Column(nullable = false, length = 20)
  private AppPlatform platform;

  @Column(name = "min_build", nullable = false)
  private int minBuild;

  @Column(name = "latest_build", nullable = false)
  private int latestBuild;

  @Column(name = "min_version", nullable = false, length = 32)
  private String minVersion;

  @Column(name = "latest_version", nullable = false, length = 32)
  private String latestVersion;

  @Column(name = "force_update", nullable = false)
  private boolean forceUpdate;

  @Column(name = "soft_update", nullable = false)
  private boolean softUpdate;

  @Column(nullable = false, length = 200)
  private String title;

  @Column(nullable = false, length = 500)
  private String message;

  @Column(name = "store_url", nullable = false, length = 1000)
  private String storeUrl;

  @Column(name = "release_notes", columnDefinition = "TEXT")
  private String releaseNotes;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;
}
