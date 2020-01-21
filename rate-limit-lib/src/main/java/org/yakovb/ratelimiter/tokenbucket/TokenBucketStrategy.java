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

  private final UserRequestDataStore<String, TokenBucket> store;
  private final Random random;

  public TokenBucketStrategy(UserRequestDataStore<String, TokenBucket> store) {
    this.store = store;
    this.random = new Random();
  }

  @Override
  public Optional<RateLimitResult> apply(Request request) {
    // TODO: outline
    // check request's user against entry in token bucket (take the bucket out of this class)
    // no entry? Make one and debit a token
    // has entry? debit token if remaining, else block and calc wait time
    String requesterId = request.getRequesterId();
    int insertionRef = random.nextInt();

    TokenBucket tokenBucket = store.computeIfAbsent(
        requesterId,
        id -> TokenBucket.builder()
            .userId(id)
            .insertionReference(insertionRef)
            .build());

    // Early return if bucket was newly inserted with our newly minted insertion ref
    if (tokenBucket.getInsertionReference() == insertionRef) {
      return Optional.empty();
    }

    // We're defo working with an existing bucket/user

    // Still got tokens
    if (tokenBucket.getRemainingTokens() > 0) {
      int remainingTokens = tokenBucket.getRemainingTokens() - 1;
      boolean limitExceeded = remainingTokens == 0;

      store.computeIfPresent(
          requesterId,
          (key, bucket) -> TokenBucket.builder()
              .userId(requesterId)
              .remainingTokens(remainingTokens)
              .exceededLimit(limitExceeded)
              .build());

      return Optional.empty();
    }

    // No more tokens
    long waitInSeconds = Duration
        .between(Instant.now(), tokenBucket.getBucketResetTime())
        .toMillis() / 1000;
    return Optional.of(new RateLimitResultImpl(waitInSeconds));
  }
}
