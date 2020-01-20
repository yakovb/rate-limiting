package org.yakovb.ratelimiter.tokenbucket;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.yakovb.ratelimiter.RequestImpl;
import org.yakovb.ratelimiter.model.RateLimitResult;
import org.yakovb.ratelimiter.model.Request;
import org.yakovb.ratelimiter.model.UserRequestDataStore;

//TODO example tests, also do a property test
// javadoc: point is to describe the intended behaviour of the class
public class TokenBucketStrategyTest {

  private Map<String, TokenBucket> backingMap;
  private TokenBucketStrategy strategy;

  @Before
  public void before() {
    backingMap = new HashMap<>();
    strategy = new TokenBucketStrategy(new InMemoryTokenBucketStore(backingMap));
  }

  @Test(expected = IllegalArgumentException.class)
  public void emptyIdThrowsException() {
    strategy.apply(new RequestImpl("", Instant.now()));
  }

  //TODO no previous entry creates new one
  @Test
  public void absentUserDataCreatesNewEntry() {
    assertThat(backingMap).isEmpty();

    Optional<RateLimitResult> result = strategy.apply(requestWithId("x"));

    assertThat(result).isEmpty();
    assertThat(backingMap.get("x")).isNotNull();
    assertThat(backingMap).hasSize(1);
  }
  //TODO existing entry updates it ('it' being based on user id)
  //TODO has sufficient tokens debits one
  //TODO has insufficient tokens returns a limit
  //TODO has insufficient tokens returns correct wait period

  private static Request requestWithId(String id) {
    return new RequestImpl(id, Instant.now());
  }
}