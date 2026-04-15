package com.docutrack.service.impl;

import com.docutrack.dto.auth.SendOtpRequestDto;
import com.docutrack.dto.auth.SendOtpResponseDto;
import com.docutrack.dto.auth.UpdateProfileRequestDto;
import com.docutrack.dto.auth.VerifyOtpRequestDto;
import com.docutrack.dto.auth.VerifyOtpResponseDto;
import com.docutrack.entity.OtpEntity;
import com.docutrack.entity.UserEntity;
import com.docutrack.exception.BadRequestException;
import com.docutrack.exception.NotFoundException;
import com.docutrack.repository.OtpRepository;
import com.docutrack.repository.UserRepository;
import com.docutrack.service.AuthService;
import com.docutrack.util.JwtService;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class OtpAuthServiceImpl implements AuthService {

  private static final Logger log = LoggerFactory.getLogger(OtpAuthServiceImpl.class);

  private static final int OTP_LENGTH = 4;
  private static final int OTP_TTL_MINUTES = 5;
  private static final int MAX_VERIFY_ATTEMPTS = 5;
  private static final int MAX_SENDS_PER_10_MIN = 5;

  private final UserRepository userRepository;
  private final OtpRepository otpRepository;
  private final Clock clock;
  private final JwtService jwtService;

  private final SecureRandom random = new SecureRandom();

  @Override
  @Transactional
  public SendOtpResponseDto sendOtp(SendOtpRequestDto request) {
    String mobile = normalizeMobile(request.getMobileNumber());
    rateLimit(mobile);

    String otp = generateOtp();
    LocalDateTime expiry = LocalDateTime.now(clock).plusMinutes(OTP_TTL_MINUTES);

    OtpEntity entity = OtpEntity.builder()
        .mobileNumber(mobile)
        .otp(otp)
        .expiryTime(expiry)
        .isUsed(false)
        .attemptCount(0)
        .build();
    otpRepository.save(entity);

    log.info("OTP generated for mobileNumber={}", mobile);
    return SendOtpResponseDto.builder()
        .message("OTP sent successfully")
        .otp(otp) // for now (testing); remove later
        .build();
  }

  @Override
  @Transactional
  public SendOtpResponseDto resendOtp(SendOtpRequestDto request) {
    String mobile = normalizeMobile(request.getMobileNumber());
    rateLimit(mobile);

    otpRepository.findTopByMobileNumberOrderByIdDesc(mobile).ifPresent(latest -> {
      if (Boolean.FALSE.equals(latest.getIsUsed()) && latest.getExpiryTime().isAfter(LocalDateTime.now(clock))) {
        latest.setIsUsed(true);
        otpRepository.save(latest);
      }
    });

    return sendOtp(request);
  }

  @Override
  @Transactional
  public VerifyOtpResponseDto verifyOtp(VerifyOtpRequestDto request) {
    String mobile = normalizeMobile(request.getMobileNumber());
    String otp = request.getOtp();

    OtpEntity latest = otpRepository.findTopByMobileNumberOrderByIdDesc(mobile)
        .orElseThrow(() -> new BadRequestException("OTP not found. Please request a new OTP."));

    if (Boolean.TRUE.equals(latest.getIsUsed())) {
      throw new BadRequestException("OTP already used. Please request a new OTP.");
    }

    if (latest.getExpiryTime().isBefore(LocalDateTime.now(clock))) {
      throw new BadRequestException("OTP expired. Please request a new OTP.");
    }

    if (latest.getAttemptCount() >= MAX_VERIFY_ATTEMPTS) {
      throw new BadRequestException("OTP retry limit exceeded. Please request a new OTP.");
    }

    if (!latest.getOtp().equals(otp)) {
      latest.setAttemptCount(latest.getAttemptCount() + 1);
      otpRepository.save(latest);
      throw new BadRequestException("Invalid OTP");
    }

    latest.setIsUsed(true);
    otpRepository.save(latest);

    UserEntity user = userRepository.findByMobileNumber(mobile)
        .orElseGet(() -> userRepository.save(UserEntity.builder()
            .mobileNumber(mobile)
            .name(null)
            .email(null)
            .isVerified(true)
            .build()));

    if (!Boolean.TRUE.equals(user.getIsVerified())) {
      user.setIsVerified(true);
      userRepository.save(user);
    }

    return VerifyOtpResponseDto.builder()
        .id(user.getId())
        .mobileNumber(user.getMobileNumber())
        .name(user.getName())
        .accessToken(jwtService.generateAccessToken(user.getId(), user.getMobileNumber()))
        .build();
  }

  @Override
  @Transactional
  public void updateProfile(Long userId, UpdateProfileRequestDto request) {
    UserEntity user = userRepository.findById(userId)
        .orElseThrow(() -> new NotFoundException("User not found: " + userId));
    user.setName(request.getName());
    user.setEmail(request.getEmail());
    userRepository.save(user);
  }

  private String generateOtp() {
    int min = (int) Math.pow(10, OTP_LENGTH - 1);
    int max = (int) Math.pow(10, OTP_LENGTH) - 1;
    int val = random.nextInt(max - min + 1) + min;
    return String.valueOf(val);
  }

  private String normalizeMobile(String mobileNumber) {
    return mobileNumber == null ? "" : mobileNumber.trim();
  }

  private void rateLimit(String mobileNumber) {
    Instant now = Instant.now(clock);
    Instant windowStart = now.minusSeconds(10 * 60L);
    long sends = otpRepository.countByMobileNumberAndCreatedAtAfter(mobileNumber, windowStart);
    if (sends >= MAX_SENDS_PER_10_MIN) {
      throw new BadRequestException("Too many OTP requests. Please try again later.");
    }
  }
}

