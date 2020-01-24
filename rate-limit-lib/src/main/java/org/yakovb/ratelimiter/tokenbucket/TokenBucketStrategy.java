package org.yakovb.ratelimiter.tokenbucket;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.Random;
import java.util.function.BiFunction;
import org.yakovb.ratelimiter.genericimpl.RateLimitResultImpl;
import org.yakovb.ratelimiter.model.RateLimitResult;
import org.yakovb.ratelimiter.model.RateLimitStrategy;
import org.yakovb.ratelimiter.model.Request;
import org.yakovb.ratelimiter.model.UserRequestDataStore;

//TODO javadoc, basic explanation of token bucket algo
public class TokenBucketStrategy implements RateLimitStrategy {

  private static final Optional<RateLimitResult> ALLOW_REQUEST = Optional.empty();
  private final UserRequestDataStore<String, TokenBucket> store;
  private final RateLimitDetails tokenBucketLimits;
  private final Random random;

  public TokenBucketStrategy(
      UserRequestDataStore<String, TokenBucket> store,
      RateLimitDetails tokenBucketLimits) {
    this.store = store;
    this.tokenBucketLimits = tokenBucketLimits;
    this.random = new Random();
  }

  @Override
  public Optional<RateLimitResult> apply(Request request) {
    TokenBucket newBucket = bucketIfFirstRequest(request);

    TokenBucket resultBucket = store.insert(
        request.getRequesterId(),
        newBucket,
        bucketIfNotFirstRequest(request));

    if (resultBucket.isExceededLimit()) {
      long waitInSeconds = computeWaitTime(request.getRequestTime(), resultBucket);
      return Optional.of(new RateLimitResultImpl(waitInSeconds));
    }
    return ALLOW_REQUEST;
  }

  private TokenBucket bucketIfFirstRequest(Request request) {
    return TokenBucket.builder()
        .userId(request.getRequesterId())
        .remainingTokens(tokenBucketLimits.getRequestsPerWindow() - 1)
        .bucketResetTime(request.getRequestTime().plus(tokenBucketLimits.getTimeWindow()))
        .exceededLimit(false)
        .build();
  }

  private BiFunction<TokenBucket, TokenBucket, TokenBucket> bucketIfNotFirstRequest(Request request) {
    return (existingBucket, notUsed) -> {

      // Should we reset the time window?
      if (request.getRequestTime().isAfter(existingBucket.getBucketResetTime())) {
        // Yes it's a new time window, so user gets a new bucket and we allow the request
        return resetBucket(request.getRequesterId(), request.getRequestTime());
      }

      // No it's an existing time window...
      if (existingBucket.getRemainingTokens() > 0) {
        // User still has tokens, so debit one and allow request
        return debitTokenFromBucket(request.getRequesterId(), existingBucket);
      }

      // By this point we know the user has no tokens and it's too early to reset the window
      // Block time
      return blockBucket(request.getRequesterId(), existingBucket);
    };
  }

  private TokenBucket resetBucket(String requesterId, Instant requestTime) {
    return TokenBucket.builder()
        .userId(requesterId)
        .remainingTokens(tokenBucketLimits.getRequestsPerWindow() - 1)
        .bucketResetTime(requestTime.plus(tokenBucketLimits.getTimeWindow()))
        .exceededLimit(false)
        .build();
  }

  private TokenBucket debitTokenFromBucket(String requesterId, TokenBucket tokenBucket) {
    int remainingTokens = tokenBucket.getRemainingTokens() - 1;
    return TokenBucket.builder()
        .userId(requesterId)
        .bucketResetTime(tokenBucket.getBucketResetTime())
        .remainingTokens(remainingTokens)
        .exceededLimit(false) // note that if remaining tokens = 0, the limit is only exceeded on the next request
        .build();
  }

  private TokenBucket blockBucket(String requesterId, TokenBucket tokenBucket) {
    return TokenBucket.builder()
        .userId(requesterId)
        .bucketResetTime(tokenBucket.getBucketResetTime())
        .remainingTokens(0)
        .exceededLimit(true)
        .build();
  }

  private static long computeWaitTime(Instant requestTime, TokenBucket tokenBucket) {
    return Duration
        .between(requestTime, tokenBucket.getBucketResetTime())
        .toMillis() / 1000;
  }
}
