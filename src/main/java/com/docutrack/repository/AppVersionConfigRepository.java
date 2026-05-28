package com.docutrack.repository;

import com.docutrack.entity.AppPlatform;
import com.docutrack.entity.AppVersionConfigEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppVersionConfigRepository extends JpaRepository<AppVersionConfigEntity, AppPlatform> {

  Optional<AppVersionConfigEntity> findByPlatform(AppPlatform platform);
}
