package org.yakovb.ratelimiter.tokenbucket;

import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.yakovb.ratelimiter.model.UserRequestDataStore;

public class InMemoryTokenBucketStore implements UserRequestDataStore<String, TokenBucket> {

  private final Map<String, TokenBucket> store;

  public InMemoryTokenBucketStore(Map<String, TokenBucket> store) {
    this.store = store;
  }

  @Override
  public TokenBucket computeIfAbsent(
      String key,
      Function<? super String, ? extends TokenBucket> updateFunction) {

    return store.computeIfAbsent(key, updateFunction);
  }

  @Override
  public Optional<TokenBucket> computeIfPresent(
      String key,
      BiFunction<? super String, ? super TokenBucket, ? extends TokenBucket> updateFunction) {

    return Optional.ofNullable(store.computeIfPresent(key, updateFunction));
  }
}
