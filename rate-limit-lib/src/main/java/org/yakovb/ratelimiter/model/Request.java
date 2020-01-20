package org.yakovb.ratelimiter.model;

import java.time.Instant;

public interface Request {

  String getRequesterId();

  Instant getRequestTime();
}
