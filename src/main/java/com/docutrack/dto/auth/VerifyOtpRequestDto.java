package com.docutrack.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class VerifyOtpRequestDto {
  @NotBlank
  @Pattern(regexp = "^[0-9]{10,15}$", message = "mobileNumber must be 10 to 15 digits")
  private String mobileNumber;

  @NotBlank
  @Pattern(regexp = "^[0-9]{4}$", message = "otp must be 4 digits")
  private String otp;
}

