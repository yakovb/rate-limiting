package org.yakovb.ratelimiter.tokenbucket.generators;

import com.pholser.junit.quickcheck.generator.GenerationStatus;
import com.pholser.junit.quickcheck.generator.Generator;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;
import java.time.Duration;
import java.time.Instant;
import org.yakovb.ratelimiter.tokenbucket.TokenBucket;

public class TokenBucketGenerator extends Generator<TokenBucket> {

  public TokenBucketGenerator() {
    super(TokenBucket.class);
  }

  @Override
  public TokenBucket generate(SourceOfRandomness random, GenerationStatus status) {
    String userId = Integer.toString(random.nextInt(1, 5));
    int insertionRef = random.nextInt(1, 10);
    int tokens = random.nextInt(0, 3);
    Duration window = random.nextDuration(Duration.ofMillis(1), Duration.ofMillis(100));

    return TokenBucket.builder()
        .userId(userId)
        .insertionReference(insertionRef)
        .remainingTokens(tokens)
        .bucketResetTime(Instant.now().plus(window))
        .exceededLimit(tokens == 0)
        .build();
  }
}
