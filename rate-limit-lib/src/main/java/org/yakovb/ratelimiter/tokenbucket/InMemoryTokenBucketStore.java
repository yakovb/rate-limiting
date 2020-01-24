package org.yakovb.ratelimiter.tokenbucket;

import java.util.Map;
import java.util.function.BiFunction;
import org.yakovb.ratelimiter.model.UserRequestDataStore;

public class InMemoryTokenBucketStore implements UserRequestDataStore<String, TokenBucket> {

  private final Map<String, TokenBucket> store;

  public InMemoryTokenBucketStore(Map<String, TokenBucket> store) {
    this.store = store;
  }

  @Override
  public TokenBucket insert(
      String userId,
      TokenBucket bucketIfNewKey,
      BiFunction<? super TokenBucket, ? super TokenBucket, ? extends TokenBucket> updateFunctionIfExistingKey) {

    return store.merge(userId, bucketIfNewKey, updateFunctionIfExistingKey);
  }
}
