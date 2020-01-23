package org.yakovb.ratelimiter.tokenbucket;

import java.time.Duration;
import lombok.Getter;

@Getter
public class RateLimitDetails {

  private final Duration timeWindow;
  private final int requestsPerWindow;

  public RateLimitDetails(Duration timeWindow, int requestsPerWindow) {
    this.timeWindow = timeWindow;
    this.requestsPerWindow = requestsPerWindow;
  }
}
