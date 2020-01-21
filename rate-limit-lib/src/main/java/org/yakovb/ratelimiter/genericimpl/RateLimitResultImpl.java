package org.yakovb.ratelimiter.genericimpl;

import org.yakovb.ratelimiter.model.RateLimitResult;

public class RateLimitResultImpl implements RateLimitResult {

  private final long waitInSeconds;

  public RateLimitResultImpl(long waitInSeconds) {
    this.waitInSeconds = waitInSeconds;
  }

  @Override
  public String getMessage() {
    return String.format("Rate limit exceeded. Try again in %d seconds", waitInSeconds);
  }
}
