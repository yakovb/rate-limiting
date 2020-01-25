package org.yakovb.ratelimiter.tokenbucket;

import java.time.Instant;
import lombok.Builder;
import lombok.Getter;

/**
 * Representation of a token bucket, tracking all the items necessary to make a block/allow decision for a request.
 */
@Getter
@Builder
public class TokenBucket {

  private final String userId;
  private final int remainingTokens;
  private final Instant bucketResetTime;
  private final boolean exceededLimit;
}
