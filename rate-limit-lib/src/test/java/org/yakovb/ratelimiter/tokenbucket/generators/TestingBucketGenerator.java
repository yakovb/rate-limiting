package org.yakovb.ratelimiter.tokenbucket.generators;

import com.pholser.junit.quickcheck.generator.GenerationStatus;
import com.pholser.junit.quickcheck.generator.Generator;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;
import java.time.Duration;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.yakovb.ratelimiter.tokenbucket.TokenBucket;
import org.yakovb.ratelimiter.tokenbucket.TokenBucketLimits;
import org.yakovb.ratelimiter.tokenbucket.generators.TestingBucketGenerator.TestingBucketBundle;

public class TestingBucketGenerator extends Generator<TestingBucketBundle> {

  public TestingBucketGenerator() {
    super(TestingBucketBundle.class);
  }

  @Override
  public TestingBucketBundle generate(SourceOfRandomness random, GenerationStatus status) {
    TokenBucketLimits limits = gen().oneOf(new TokenBucketLimitsGenerator()).generate(random, status);

    String userId = Integer.toString(random.nextInt(1, 5));
    int insertionRef = random.nextInt(1, 10);
    int tokens = random.nextInt(0, limits.getTokensPerWindow());
    Instant resetTime = calcResetTime(limits, random);

    TokenBucket bucket = TokenBucket.builder()
        .userId(userId)
        .insertionReference(insertionRef)
        .remainingTokens(tokens)
        .bucketResetTime(resetTime)
        .exceededLimit(tokens == 0)
        .build();

    return new TestingBucketBundle(bucket, limits);
  }

  private static Instant calcResetTime(TokenBucketLimits limits, SourceOfRandomness random) {
    Instant now = Instant.now();
    Duration offset = limits.getTimeWindow().dividedBy(2);
    return random.nextBoolean() ? now.plus(offset) : now.minus(offset);
  }


  @Getter
  @AllArgsConstructor
  public static class TestingBucketBundle {
    private final TokenBucket bucket;
    private final TokenBucketLimits limits;
  }
}
