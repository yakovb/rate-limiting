package org.yakovb.ratelimiter.genericimpl;

import java.time.Instant;
import java.util.Objects;
import lombok.Getter;
import org.yakovb.ratelimiter.model.Request;

@Getter
public class RequestImpl implements Request {

  private final String requesterId;
  private final Instant requestTime;

  public RequestImpl(String requesterId, Instant requestTime) {
    Objects.requireNonNull(requesterId, "Requester ID cannot be null");
    Objects.requireNonNull(requestTime, "Request time cannot be null");
    ensureNotEmpty(requesterId);

    this.requesterId = requesterId;
    this.requestTime = requestTime;
  }

  private void ensureNotEmpty(String requesterId) {
    if (requesterId.trim().isEmpty()) {
      throw new IllegalArgumentException("Requester ID cannot be empty");
    }
  }
}
