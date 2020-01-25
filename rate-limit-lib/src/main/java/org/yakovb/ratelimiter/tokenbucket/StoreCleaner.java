package org.yakovb.ratelimiter.tokenbucket;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import lombok.extern.slf4j.Slf4j;

/**
 * Utility class to remove stale entries from the token bucket data store. This deals with the case of users "dropping off"
 * and leaving their request-tracking data in the store forever. It works based on a schedule, finding token buckets
 * that have not been updated in a given length of time.
 */
@Slf4j
public class StoreCleaner {

  private static final boolean IS_DAEMON = true;
  private static final long THIRTY_MINUTES = Duration.ofMinutes(30).toMillis();

  public void startCleaningInBackground(Map<String, TokenBucket> store) {
    log.debug("Starting token bucket store cleaner...");
    Timer timer = new Timer(IS_DAEMON);
    timer.scheduleAtFixedRate(
        new CleanerTask(store),
        THIRTY_MINUTES,
        THIRTY_MINUTES);
  }


  static class CleanerTask extends TimerTask {
    private static final Duration INACTIVITY_PERIOD = Duration.ofHours(2);
    private static final TokenBucket REMOVE_BUCKET = null;

    private final Map<String, TokenBucket> store;

    CleanerTask(Map<String, TokenBucket> store) {
      this.store = store;
    }

    @Override
    public void run() {
      log.debug("Executing a store clean");
      store.forEach((userId, bucket) -> {

        if (bucketNotResetInLast(bucket, INACTIVITY_PERIOD)) {

          store.computeIfPresent(
              bucket.getUserId(),
              (key, data) -> REMOVE_BUCKET);
        }
      });
    }

    private static boolean bucketNotResetInLast(TokenBucket bucket, Duration duration) {
      Instant cutOffTime = Instant.now().minus(duration);
      return bucket.getBucketResetTime().isBefore(cutOffTime);
    }
  }
}
