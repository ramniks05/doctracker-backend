package com.docutrack.dto.admin;

import jakarta.validation.constraints.NotBlank;
import lombok.Value;

@Value
public class AdminLoginRequestDto {
  @NotBlank String username;
  @NotBlank String password;
}
