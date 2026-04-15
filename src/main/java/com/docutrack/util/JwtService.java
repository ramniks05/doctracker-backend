package com.docutrack.util;

import com.docutrack.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

@Component
public class JwtService {

  public static final String CLAIM_MOBILE = "mobileNumber";

  private final JwtProperties props;
  private final Clock clock;

  public JwtService(JwtProperties props, Clock clock) {
    this.props = props;
    this.clock = clock;
  }

  public String generateAccessToken(Long userId, String mobileNumber) {
    Instant now = Instant.now(clock);
    Instant exp = now.plus(props.accessTokenTtl());
    SecretKey key = signingKey();

    return Jwts.builder()
        .subject(String.valueOf(userId))
        .issuedAt(Date.from(now))
        .expiration(Date.from(exp))
        .claim(CLAIM_MOBILE, mobileNumber)
        .signWith(key, Jwts.SIG.HS256)
        .compact();
  }

  public Claims parseAndValidate(String token) {
    SecretKey key = signingKey();
    return (Claims) Jwts.parser()
        .verifyWith(key)
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }

  private SecretKey signingKey() {
    byte[] bytes = props.secret().getBytes(StandardCharsets.UTF_8);
    return Keys.hmacShaKeyFor(bytes);
  }
}

