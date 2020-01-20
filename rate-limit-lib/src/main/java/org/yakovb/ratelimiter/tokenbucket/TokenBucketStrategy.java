package org.yakovb.ratelimiter.tokenbucket;

import java.util.Optional;
import org.yakovb.ratelimiter.model.RateLimitResult;
import org.yakovb.ratelimiter.model.RateLimitStrategy;
import org.yakovb.ratelimiter.model.Request;

public class TokenBucketStrategy implements RateLimitStrategy {

  @Override
  public Optional<RateLimitResult> apply(Request request) {
    // TODO: outline
    // check request's user against entry in token bucket (take the bucket out of this class)
    // no entry? Make one and debit a token
    // has entry? debit token if remaining, else block and calc wait time
    return Optional.empty();
  }
}
