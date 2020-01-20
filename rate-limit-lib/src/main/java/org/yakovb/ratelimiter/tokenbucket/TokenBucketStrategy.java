package org.yakovb.ratelimiter.tokenbucket;

import java.util.Optional;
import org.yakovb.ratelimiter.model.RateLimitResult;
import org.yakovb.ratelimiter.model.RateLimitStrategy;
import org.yakovb.ratelimiter.model.Request;
import org.yakovb.ratelimiter.model.UserRequestDataStore;

//TODO javadoc, basic explanation of token bucket algo
public class TokenBucketStrategy implements RateLimitStrategy {

  private final UserRequestDataStore<String, TokenBucket> store;

  public TokenBucketStrategy(UserRequestDataStore<String, TokenBucket> store) {
    this.store = store;
  }

  @Override
  public Optional<RateLimitResult> apply(Request request) {
    // TODO: outline
    // check request's user against entry in token bucket (take the bucket out of this class)
    // no entry? Make one and debit a token
    // has entry? debit token if remaining, else block and calc wait time
    return Optional.empty();
  }
}
