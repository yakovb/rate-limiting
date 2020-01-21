package org.yakovb.ratelimiter.tokenbucket;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.yakovb.ratelimiter.genericimpl.RequestImpl;
import org.yakovb.ratelimiter.model.RateLimitResult;
import org.yakovb.ratelimiter.model.Request;

//TODO example tests, also do a property test
// javadoc: point is to describe the intended behaviour of the class
public class TokenBucketStrategyTest {

  private static final int INSERTION_REFERENCE = 123;
  public static final String USER_ID = "x";

  private Map<String, TokenBucket> backingMap;
  private TokenBucketLimits limits;
  private TokenBucketStrategy strategy;

  @Before
  public void before() {
    backingMap = new HashMap<>();
    limits = new TokenBucketLimits(Duration.ofMinutes(1), 50);
    strategy = new TokenBucketStrategy(new InMemoryTokenBucketStore(backingMap), limits);
  }

  @Test(expected = IllegalArgumentException.class)
  public void emptyIdThrowsException() {
    strategy.apply(new RequestImpl("", Instant.now()));
  }

  @Test
  public void absentUserDataCreatesNewEntry() {
    assertThat(backingMap).isEmpty();

    Optional<RateLimitResult> result = strategy.apply(requestWithId(USER_ID));

    assertThat(result).isEmpty();
    assertThat(backingMap).hasSize(1);
    assertThat(backingMap.get(USER_ID)).isNotNull();
  }

  @Test
  public void newlyCreatedUserHasCorrectTokenCount() {
    assertThat(backingMap).isEmpty();

    Optional<RateLimitResult> result = strategy.apply(requestWithId(USER_ID));

    assertThat(result).isEmpty();
    assertThat(backingMap).hasSize(1);

    TokenBucket userBucket = backingMap.get(USER_ID);
    assertThat(userBucket).isNotNull();
    assertThat(userBucket.getRemainingTokens()).isEqualTo(limits.getTokensPerWindow() - 1);
  }

  @Test
  public void existingUserHasTokensDebitedUponNewRequest() {
    int existingTokens = 2;
    backingMap.put(USER_ID, bucketWithIdAndTokens(USER_ID, existingTokens));

    Optional<RateLimitResult> result = strategy.apply(requestWithId(USER_ID));
    TokenBucket bucket = backingMap.get(USER_ID);

    assertThat(result).isEmpty();
    assertThat(backingMap).hasSize(1);
    assertThat(bucket.getRemainingTokens()).isEqualTo(existingTokens - 1);
    assertThat(bucket.getInsertionReference()).isNotEqualTo(INSERTION_REFERENCE);
  }

  @Test
  public void existingUserWithInsufficientTokensGetsBlockedWithWait() {
    backingMap.put(USER_ID, bucketWithIdAndTokensAndReset(USER_ID, 0, Instant.now().plusSeconds(10)));

    Optional<RateLimitResult> result = strategy.apply(requestWithId(USER_ID));
    TokenBucket bucket = backingMap.get(USER_ID);

    assertThat(backingMap).hasSize(1);
    assertThat(bucket.getRemainingTokens()).isEqualTo(0);
    assertThat(bucket.getInsertionReference()).isEqualTo(INSERTION_REFERENCE);

    assertThat(result).isNotEmpty();
    RateLimitResult rateLimitResult = result.get();
    assertThat(rateLimitResult.getMessage()).containsSubsequence(
        "Rate limit exceeded. Try again in 10 seconds");
  }

  @Test
  public void tokensReachingZeroSetsLimitExceededFlag() {
    backingMap.put(USER_ID, bucketWithIdAndTokens(USER_ID, 1));

    Optional<RateLimitResult> result = strategy.apply(requestWithId(USER_ID));
    TokenBucket bucket = backingMap.get(USER_ID);

    assertThat(result).isEmpty();
    assertThat(backingMap).hasSize(1);
    assertThat(bucket.getRemainingTokens()).isEqualTo(0);
    assertThat(bucket.isExceededLimit()).isTrue();
  }

  //TODO reset on new wait period and tokens zero
  //TODO reset on new wait period and tokens > zero

  private static Request requestWithId(String id) {
    return new RequestImpl(id, Instant.now());
  }

  private static TokenBucket bucketWithIdAndTokens(String id, int tokens) {
    return TokenBucket.builder()
        .userId(id)
        .remainingTokens(tokens)
        .insertionReference(INSERTION_REFERENCE)
        .build();
  }

  private static TokenBucket bucketWithIdAndTokensAndReset(String id, int tokens, Instant reset) {
    return TokenBucket.builder()
        .userId(id)
        .remainingTokens(tokens)
        .bucketResetTime(reset)
        .insertionReference(INSERTION_REFERENCE)
        .build();
  }
}