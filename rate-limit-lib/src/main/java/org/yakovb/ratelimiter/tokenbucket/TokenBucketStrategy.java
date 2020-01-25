package org.yakovb.ratelimiter.tokenbucket;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.function.BiFunction;
import lombok.extern.slf4j.Slf4j;
import org.yakovb.ratelimiter.genericimpl.RateLimitResultImpl;
import org.yakovb.ratelimiter.model.RateLimitResult;
import org.yakovb.ratelimiter.model.RateLimitStrategy;
import org.yakovb.ratelimiter.model.Request;
import org.yakovb.ratelimiter.model.UserRequestDataStore;

/**
 * The Toke Bucket rate limiting strategy works based on the notion: each user gets a certain number of tokens that they
 * are allowed to use in a given time period. Each request "uses up" a token. If a user has no more tokens when they
 * make a request, it's denied. When the time period is up, the bucket "resets", i.e. fills up with a fresh batch of tokens.
 */
@Slf4j
public class TokenBucketStrategy implements RateLimitStrategy {

  private final UserRequestDataStore<String, TokenBucket> store;
  private final RateLimitDetails tokenBucketLimits;

  public TokenBucketStrategy(
      UserRequestDataStore<String, TokenBucket> store,
      RateLimitDetails tokenBucketLimits) {
    this.store = store;
    this.tokenBucketLimits = tokenBucketLimits;
  }

  @Override
  public Optional<RateLimitResult> apply(Request request) {
    log.info("Processing request for {} at {}", request.getRequesterId(), request.getRequestTime().toString());
    TokenBucket newBucket = bucketForFirstRequest(request);

    TokenBucket resultBucket = store.insert(
        request.getRequesterId(),
        newBucket,
        bucketForSubsequentRequests(request));

    if (resultBucket.isExceededLimit()) {
      long waitInSeconds = computeWaitTime(request.getRequestTime(), resultBucket);
      log.info("Request for {} blocked: rate limit exceeded. Retry in {} seconds",
          request.getRequesterId(),
          waitInSeconds);
      return Optional.of(new RateLimitResultImpl(waitInSeconds));
    }

    return Optional.empty();
  }

  // The token bucket to insert if this is the user's first request for the time window
  private TokenBucket bucketForFirstRequest(Request request) {
    return TokenBucket.builder()
        .userId(request.getRequesterId())
        .remainingTokens(tokenBucketLimits.getRequestsPerWindow() - 1)
        .bucketResetTime(request.getRequestTime().plus(tokenBucketLimits.getTimeWindow()))
        .exceededLimit(false)
        .build();
  }

  // The function to compute the bucket for insertion if the user has already made at least one request in the time window
  private BiFunction<TokenBucket, TokenBucket, TokenBucket> bucketForSubsequentRequests(Request request) {
    return (existingBucket, notUsed) -> {

      // Should we reset the time window?
      if (request.getRequestTime().isAfter(existingBucket.getBucketResetTime())) {
        // Yes it's a new time window, so user gets a new bucket and we allow the request
        log.info("Request for {} allowed. Reset time window, debit new bucket", request.getRequesterId());
        return bucketForFirstRequest(request);
      }

      // No it's an existing time window...
      if (existingBucket.getRemainingTokens() > 0) {
        // User still has tokens, so debit one and allow request
        log.info("Request for {} allowed. Debit bucket", request.getRequesterId());
        return debitTokenFromBucket(request.getRequesterId(), existingBucket);
      }

      // By this point we know the user has no tokens and it's too early to reset the window
      // Block time
      log.info("Request for {} blocked", request.getRequesterId());
      return blockBucket(request.getRequesterId(), existingBucket);
    };
  }

  private static TokenBucket debitTokenFromBucket(String requesterId, TokenBucket tokenBucket) {
    int remainingTokens = tokenBucket.getRemainingTokens() - 1;
    return TokenBucket.builder()
        .userId(requesterId)
        .bucketResetTime(tokenBucket.getBucketResetTime())
        .remainingTokens(remainingTokens)
        .exceededLimit(false) // note that if remaining tokens = 0, the limit is only exceeded on the next request
        .build();
  }

  private static TokenBucket blockBucket(String requesterId, TokenBucket tokenBucket) {
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
