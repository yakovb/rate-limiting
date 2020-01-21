package org.yakovb.ratelimiter.tokenbucket;

import java.time.Duration;
import lombok.Getter;

@Getter
public class TokenBucketLimits {

  private final Duration timeWindow;
  private final int tokensPerWindow;

  public TokenBucketLimits(Duration timeWindow, int tokensPerWindow) {
    this.timeWindow = timeWindow;
    this.tokensPerWindow = tokensPerWindow;
  }
}
