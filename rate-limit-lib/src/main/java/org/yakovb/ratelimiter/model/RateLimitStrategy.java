package org.yakovb.ratelimiter.model;

import java.util.Optional;

/**
 * Very basic interface outline what's required of every Rate Limit Strategy. The idea is to give substantial freedom
 * to implementors of this interface.
 */
public interface RateLimitStrategy {
  Optional<RateLimitResult> apply(Request request);
}
