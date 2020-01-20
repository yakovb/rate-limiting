package org.yakovb.ratelimiter.model;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

//TODO javadoc, explain these weird method sigs - based on Map, used to simplify atomic updates in concurrent env
public interface UserRequestDataStore<KEY, DATA> {

  DATA computeIfAbsent(KEY key, Function<? super KEY, ? extends DATA> updateFunction);

  Optional<DATA> computeIfPresent(KEY key, BiFunction<? super KEY, ? super DATA, ? extends DATA> updateFunction);
}
