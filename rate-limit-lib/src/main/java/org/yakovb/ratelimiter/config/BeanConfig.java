package org.yakovb.ratelimiter.config;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.yakovb.ratelimiter.config.BeanConfig.RateLimitConfig;
import org.yakovb.ratelimiter.model.RateLimitStrategy;
import org.yakovb.ratelimiter.model.UserRequestDataStore;
import org.yakovb.ratelimiter.servlet.RateLimitFilter;
import org.yakovb.ratelimiter.tokenbucket.InMemoryTokenBucketStore;
import org.yakovb.ratelimiter.tokenbucket.RateLimitDetails;
import org.yakovb.ratelimiter.tokenbucket.StoreCleaner;
import org.yakovb.ratelimiter.tokenbucket.TokenBucket;
import org.yakovb.ratelimiter.tokenbucket.TokenBucketStrategy;

/**
 * Defines the classes that will be available to users of this library.
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(RateLimitConfig.class)
public class BeanConfig {

  private static final Duration DEFAULT_WINDOW = Duration.ofHours(1);
  private static final int DEFAULT_TOKENS = 100;

  @Autowired(required = false)
  private RateLimitConfig rateLimitConfig;

  @Bean
  public RateLimitFilter rateLimitFilter(RateLimitStrategy rateLimitStrategy) {
    return new RateLimitFilter(rateLimitStrategy);
  }

  /**
   * A default Rate Limit Strategy is provided only if users of this library don't provide one themselves.
   */
  @Bean
  @ConditionalOnMissingBean
  public RateLimitStrategy rateLimitStrategy(RateLimitDetails rateLimitDetails) {
    log.info("Rate limit details: {} requests per {} minutes",
        rateLimitDetails.getRequestsPerWindow(),
        rateLimitDetails.getTimeWindow().toMinutes());
    return new TokenBucketStrategy(userRequestDataStore(), rateLimitDetails);
  }

  /**
   * Rate limit window + allowed requests have some defaults set, if users of this library done define any in their
   * application property file.
   */
  @Bean
  public RateLimitDetails rateLimitDetails() {
    if (rateLimitConfig == null) {
      return new RateLimitDetails(DEFAULT_WINDOW, DEFAULT_TOKENS);
    }

    return new RateLimitDetails(
        Duration.ofMinutes(rateLimitConfig.getWindowMinutes()),
        rateLimitConfig.getWindowRequests());
  }

  private UserRequestDataStore<String, TokenBucket> userRequestDataStore() {
    Map<String, TokenBucket> backingMap = new ConcurrentHashMap<>();
    StoreCleaner storeCleaner = new StoreCleaner();
    storeCleaner.startCleaningInBackground(backingMap);

    return new InMemoryTokenBucketStore(backingMap);
  }


  @Getter
  @Setter
  @ConfigurationProperties(prefix = "ratelimits")
  static class RateLimitConfig {
    private int windowMinutes;
    private int windowRequests;
  }
}
