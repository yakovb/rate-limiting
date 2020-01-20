package org.yakovb.ratelimiter.model;

import java.util.Optional;

public interface RateLimitStrategy {
  Optional<RateLimitResult> apply(Request request);
}
