package com.docutrack.security;

import com.docutrack.exception.UnauthorizedException;
import com.docutrack.util.JwtService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

  private final JwtService jwtService;

  public JwtAuthFilter(JwtService jwtService) {
    this.jwtService = jwtService;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    String auth = request.getHeader("Authorization");
    if (auth == null || !auth.startsWith("Bearer ")) {
      filterChain.doFilter(request, response);
      return;
    }

    String token = auth.substring("Bearer ".length()).trim();
    try {
      Claims claims = jwtService.parseAndValidate(token);

      UserPrincipal principal;
      List<SimpleGrantedAuthority> authorities;

      if (jwtService.isAdminClaims(claims)) {
        String adminUsername = claims.get(JwtService.CLAIM_ADMIN_USERNAME, String.class);
        principal = UserPrincipal.builder()
            .admin(true)
            .adminUsername(adminUsername)
            .build();
        authorities = List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
      } else {
        Long userId = Long.valueOf(claims.getSubject());
        String mobile = claims.get(JwtService.CLAIM_MOBILE, String.class);
        principal = UserPrincipal.builder()
            .userId(userId)
            .mobileNumber(mobile)
            .build();
        authorities = List.of();
      }

      UsernamePasswordAuthenticationToken authentication =
          new UsernamePasswordAuthenticationToken(principal, null, authorities);
      authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

      SecurityContextHolder.getContext().setAuthentication(authentication);
      filterChain.doFilter(request, response);
    } catch (Exception e) {
      SecurityContextHolder.clearContext();
      throw new UnauthorizedException("Invalid or expired token");
    }
  }
}

