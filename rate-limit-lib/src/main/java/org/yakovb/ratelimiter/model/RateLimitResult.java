package org.yakovb.ratelimiter.model;

import java.time.Duration;

/**
 * The 'block' result returned from applying a Rate Limit Strategy.
 */
public interface RateLimitResult {

  int HTTP_CODE_TOO_MANY_REQUESTS = 429;

  /**
   * HTTP response code should normally be 429, but this is overridable.
   */
  default int getHttpCode() {
    return HTTP_CODE_TOO_MANY_REQUESTS;
  }

  /**
   * The HTTP response message to return when the rate limit is exceeded.
   */
  String getMessage();

  /**
   * The length of time a user has to wait before making another request. Implementors are free to choose their own
   * time unit.
   */
  Duration getWaitDuration();
}
