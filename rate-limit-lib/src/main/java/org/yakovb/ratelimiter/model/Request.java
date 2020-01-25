package org.yakovb.ratelimiter.model;

import java.time.Instant;

/**
 * Minimum required information that a request should contain in order to be usable by a Rate Limit Strategy.
 */
public interface Request {

  String getRequesterId();

  Instant getRequestTime();
}
