package com.docutrack.repository.projection;

public interface UserDocumentCountProjection {
  Long getUserId();
  Long getTotal();
  Long getActive();
  Long getExpired();
  Long getExpiringSoon();
}
