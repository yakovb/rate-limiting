package org.yakovb.ratelimiter.model;

public interface RateLimitResult {

  int HTTP_CODE_TOO_MANY_REQUESTS = 429;

  default int getHttpCode() {
    return HTTP_CODE_TOO_MANY_REQUESTS;
  }

  String getMessage();
}
