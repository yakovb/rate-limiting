package org.yakovb.ratelimiter.tokenbucket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.yakovb.ratelimiter.tokenbucket.generators.TestingBucketGenerator.TestingBucketBundle;

import com.pholser.junit.quickcheck.From;
import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.generator.InRange;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.junit.runner.RunWith;
import org.yakovb.ratelimiter.genericimpl.RequestImpl;
import org.yakovb.ratelimiter.tokenbucket.generators.TestingBucketGenerator;

/**
 * Property-based test of the Token Bucket Strategy. This class specifies key properties of the Strategy and the
 * conditions for which they must be true.
 */
@RunWith(JUnitQuickcheck.class)
public class TokenBucketStrategyPropertyTest {

  @Property
  public void tokenCountNeverAboveMaxLimit(
      @From(TestingBucketGenerator.class) TestingBucketBundle testingBundle,
      @InRange(minInt = -100, maxInt = 100) int offsetFromNow) {

    TokenBucket bucket = testingBundle.getBucket();
    RateLimitDetails limits = testingBundle.getLimits();

    Map<String, TokenBucket> backingMap = createMapWithBucket(bucket);
    TokenBucketStrategy strategy = createStrategy(backingMap, limits);
    strategy.apply(createRequest(bucket, offsetFromNow));

    assertThat(backingMap.get(bucket.getUserId()).getRemainingTokens()).isLessThanOrEqualTo(limits.getRequestsPerWindow());
  }

  @Property
  public void tokenCountNeverDropsBelowZero(
      @From(TestingBucketGenerator.class) TestingBucketBundle testingBundle,
      @InRange(minInt = -100, maxInt = 100) int offsetFromNow) {

    TokenBucket bucket = testingBundle.getBucket();
    RateLimitDetails limits = testingBundle.getLimits();

    Map<String, TokenBucket> backingMap = createMapWithBucket(bucket);
    TokenBucketStrategy strategy = createStrategy(backingMap, limits);
    strategy.apply(createRequest(bucket, offsetFromNow));

    assertThat(backingMap.get(bucket.getUserId()).getRemainingTokens()).isGreaterThanOrEqualTo(0);
  }

  @Property
  public void neverDebitMoreThanOneToken(
      @From(TestingBucketGenerator.class) TestingBucketBundle testingBundle,
      @InRange(minInt = -100, maxInt = 100) int offsetFromNow) {

    TokenBucket bucket = testingBundle.getBucket();
    RateLimitDetails limits = testingBundle.getLimits();

    Map<String, TokenBucket> backingMap = createMapWithBucket(bucket);
    TokenBucketStrategy strategy = createStrategy(backingMap, limits);
    strategy.apply(createRequest(bucket, offsetFromNow));

    int tokensBefore = bucket.getRemainingTokens();
    int remainingTokens = backingMap.get(bucket.getUserId()).getRemainingTokens();

    if (tokensBefore > 0) {
      assertThat(remainingTokens).isIn(
          tokensBefore - 1,                   // debit single token
          limits.getRequestsPerWindow() - 1); // reset window and debit single token
    }
    if (tokensBefore == 0) {
      assertThat(remainingTokens).isIn(
          0,                                  // blocked, so tokens stay at 0
          limits.getRequestsPerWindow() - 1); // reset window and debit single token
    }
  }

  private static TokenBucketStrategy createStrategy(Map<String, TokenBucket> map, RateLimitDetails limits) {
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