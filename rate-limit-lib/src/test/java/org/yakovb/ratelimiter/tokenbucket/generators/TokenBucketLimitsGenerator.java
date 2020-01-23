package org.yakovb.ratelimiter.tokenbucket.generators;

import com.pholser.junit.quickcheck.generator.GenerationStatus;
import com.pholser.junit.quickcheck.generator.Generator;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;
import java.time.Duration;
import org.yakovb.ratelimiter.tokenbucket.RateLimitDetails;

public class TokenBucketLimitsGenerator extends Generator<RateLimitDetails> {

  public TokenBucketLimitsGenerator() {
    super(RateLimitDetails.class);
  }

  @Override
  public RateLimitDetails generate(SourceOfRandomness random, GenerationStatus status) {
    Duration window = random.nextDuration(Duration.ofMillis(1), Duration.ofMillis(100));
    int tokens = random.nextInt(1, 3);

    return new RateLimitDetails(window, tokens);
  }
}
