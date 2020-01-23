package org.yakovb.ratelimiter.tokenbucket;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.Random;
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
    // Init some bookkeeping values
    String requesterId = request.getRequesterId();
    int insertionRef = random.nextInt();
    Instant now = Instant.now(); //TODO use instant from the request!!!

    // Assume we have to create a new bucket for a new user
    TokenBucket tokenBucket = createNewBucket(requesterId, insertionRef, now);
    if (newBucketWasCreated(insertionRef, tokenBucket)) {
      return ALLOW_REQUEST;
    }
    // We now know we're working with an existing bucket and user...

    if (now.isAfter(tokenBucket.getBucketResetTime())) {
      // New time window, so user gets a new bucket and we allow the request
      resetBucket(requesterId, insertionRef, now);
      return ALLOW_REQUEST;
    }

    if (tokenBucket.getRemainingTokens() > 0) {
      // User still has tokens, so debit one and allow request
      debitTokenBucket(requesterId, insertionRef, tokenBucket);
      return ALLOW_REQUEST;
    }

    // By this point we know the user has no tokens and it's too early to reset the window
    // Block time
    long waitInSeconds = computeWaitTime(now, tokenBucket);
    return Optional.of(new RateLimitResultImpl(waitInSeconds));
  }

  private static boolean newBucketWasCreated(int insertionRef, TokenBucket tokenBucket) {
    return tokenBucket.getInsertionReference() == insertionRef;
  }

  private TokenBucket createNewBucket(String requesterId, int insertionRef, Instant now) {
    return store.computeIfAbsent(
        requesterId,
        id -> TokenBucket.builder()
            .userId(id)
            .insertionReference(insertionRef)
            .remainingTokens(tokenBucketLimits.getRequestsPerWindow() - 1)
            .bucketResetTime(now.plus(tokenBucketLimits.getTimeWindow()))
            .exceededLimit(false)
            .build());
  }

  private void resetBucket(String requesterId, int insertionRef, Instant now) {
    store.computeIfPresent(
        requesterId,
        (key, bucket) -> TokenBucket.builder()
            .userId(requesterId)
            .insertionReference(insertionRef)
            .remainingTokens(tokenBucketLimits.getRequestsPerWindow() - 1)
            .bucketResetTime(now.plus(tokenBucketLimits.getTimeWindow()))
            .exceededLimit(false)
            .build());
  }

  private void debitTokenBucket(String requesterId, int insertionRef, TokenBucket tokenBucket) {
    int remainingTokens = tokenBucket.getRemainingTokens() - 1;
    boolean limitExceeded = remainingTokens == 0;
    store.computeIfPresent(
        requesterId,
        (key, bucket) -> TokenBucket.builder()
            .userId(requesterId)
            .insertionReference(insertionRef)
            .bucketResetTime(bucket.getBucketResetTime())
            .remainingTokens(remainingTokens)
            .exceededLimit(limitExceeded)
            .build());
  }

  private static long computeWaitTime(Instant now, TokenBucket tokenBucket) {
    return Duration
        .between(now, tokenBucket.getBucketResetTime())
        .toMillis() / 1000;
  }
}
