package org.yakovb.ratelimiter.model;

import java.util.function.BiFunction;

//TODO javadoc, explain these weird method sigs - based on Map, used to simplify atomic updates in concurrent env
public interface UserRequestDataStore<KEY, DATA> {

  DATA insert(KEY key, DATA dataIfNewKey, BiFunction<? super DATA, ? super DATA, ? extends DATA> updateFunctionIfExistingKey);
}
