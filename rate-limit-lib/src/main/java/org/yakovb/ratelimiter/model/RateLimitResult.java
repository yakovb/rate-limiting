package org.yakovb.ratelimiter.model;

import java.time.Duration;

public interface RateLimitResult {

  int HTTP_CODE_TOO_MANY_REQUESTS = 429;

  default int getHttpCode() {
    return HTTP_CODE_TOO_MANY_REQUESTS;
  }

  String getMessage();

  Duration getWaitDuration();
}
