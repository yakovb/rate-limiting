package org.yakovb.ratelimiter.model;

import java.util.function.BiFunction;

/**
 * To compute whether a request should be allowed or blocked, we need to remember past requests, hence we need to store
 * them. This request data store is how we do that.
 *
 * @param <KEY> The type of the key that indexes a user's request data in the store.
 * @param <DATA> The type of the data held by the store.
 */
public interface UserRequestDataStore<KEY, DATA> {

  /**
   * Only one method is exposed by this store, but it allows for inserting new data and updating existing data. The
   * purpose of having a single method is to encourage atomic updates by implementors of this class. That is, the
   * decision as to whether to insert new data or update existing data is made in a single atomic method, thus
   * reducing the chance of race conditions when multiple clients use the store.
   *
   * @param key The key to index into the store.
   * @param dataIfNewKey Data to insert if the key isn't in the store.
   * @param updateFunctionIfExistingKey Function to execute if the key is in the store. This is a function of two
   * arguments: the first is the data retrieved from the store, the second is the "new" data to insert. The function is
   * free to use one or both or these objects, or combine them in some way, when executing an insert.
   *
   * @return the data that was inserted into the store.
   */
  DATA insert(KEY key, DATA dataIfNewKey, BiFunction<? super DATA, ? super DATA, ? extends DATA> updateFunctionIfExistingKey);
}
