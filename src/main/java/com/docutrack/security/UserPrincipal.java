package com.docutrack.security;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UserPrincipal {
  Long userId;
  String mobileNumber;
}

