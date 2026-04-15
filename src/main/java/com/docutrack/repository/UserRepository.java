package com.docutrack.repository;

import com.docutrack.entity.UserEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
  Optional<UserEntity> findByMobileNumber(String mobileNumber);
}

