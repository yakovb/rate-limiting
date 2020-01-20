package org.yakovb.ratelimiter.tokenbucket;

//TODO javadoc: explain purpose of property tests for this class
// ie ensure invariants hold
public class TokenBucketStrategyPropertyTest {

  //TODO no bucket ever has more than max tokens
  //TODO no bucket ever has less than zero tokens
  //TODO no debit event removes more than one token
  //TODO empty buckets stay empty until reset time
  //TODO empty bucket time remaining always accurate += 1 second
}