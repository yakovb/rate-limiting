package org.yakovb.ratelimiter.tokenbucket;

import java.time.Instant;
import org.junit.Test;
import org.yakovb.ratelimiter.RequestImpl;

//TODO example tests, also do a property test
// javadoc: point is to describe the intended behaviour of the class
public class TokenBucketStrategyTest {

  private TokenBucketStrategy strategy = new TokenBucketStrategy();

  @Test(expected = IllegalArgumentException.class)
  public void emptyIdThrowsException() {
    strategy.apply(new RequestImpl("", Instant.now()));
  }

  //TODO no previous entry creates new one
  //TODO existing entry updates it ('it' being based on user id)
  //TODO has sufficient tokens debits one
  //TODO has insufficient tokens returns a limit
  //TODO has insufficient tokens returns correct wait period
}