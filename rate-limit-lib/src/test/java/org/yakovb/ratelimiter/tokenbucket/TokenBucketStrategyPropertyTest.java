package org.yakovb.ratelimiter.tokenbucket;

import static org.assertj.core.api.Assertions.assertThat;

import com.pholser.junit.quickcheck.From;
import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.generator.InRange;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.junit.runner.RunWith;
import org.yakovb.ratelimiter.genericimpl.RequestImpl;
import org.yakovb.ratelimiter.tokenbucket.generators.TokenBucketGenerator;
import org.yakovb.ratelimiter.tokenbucket.generators.TokenBucketLimitsGenerator;

//TODO javadoc: explain purpose of property tests for this class
// ie ensure invariants hold
@RunWith(JUnitQuickcheck.class)
public class TokenBucketStrategyPropertyTest {

  //TODO no bucket ever has more than max tokens
  @Property
  public void tokenCountNeverDropsBelowZero(
      @From(TokenBucketGenerator.class) TokenBucket bucket,
      @From(TokenBucketLimitsGenerator.class) TokenBucketLimits limits,
      @InRange(minInt = -100, maxInt = 100) int offsetFromNow) {

    Map<String, TokenBucket> backingMap = createMapWithBucket(bucket);
    TokenBucketStrategy strategy = createStrategy(backingMap, limits);
    strategy.apply(createRequest(bucket, offsetFromNow));

    assertThat(backingMap.get(bucket.getUserId()).getRemainingTokens()).isGreaterThanOrEqualTo(0);
  }

  //TODO no bucket ever has less than zero tokens
  //TODO no debit event removes more than one token
  //TODO empty buckets stay empty until reset time
  //TODO empty bucket time remaining always accurate += 1 second

  private static TokenBucketStrategy createStrategy(Map<String, TokenBucket> map, TokenBucketLimits limits) {
    return new TokenBucketStrategy(
        new InMemoryTokenBucketStore(map),
        limits);
  }

  private static RequestImpl createRequest(TokenBucket bucket, int offset) {
    return new RequestImpl(
        bucket.getUserId(),
        Instant.now().plusMillis(offset));
  }

  private static Map<String, TokenBucket> createMapWithBucket(TokenBucket bucket) {
    Map<String, TokenBucket> backingMap = new HashMap<>();
    backingMap.put(bucket.getUserId(), bucket);
    return backingMap;
  }
}