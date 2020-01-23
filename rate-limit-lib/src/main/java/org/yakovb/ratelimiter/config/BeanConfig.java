package org.yakovb.ratelimiter.config;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.yakovb.ratelimiter.config.BeanConfig.RateLimitConfig;
import org.yakovb.ratelimiter.model.RateLimitStrategy;
import org.yakovb.ratelimiter.model.UserRequestDataStore;
import org.yakovb.ratelimiter.tokenbucket.InMemoryTokenBucketStore;
import org.yakovb.ratelimiter.tokenbucket.RateLimitDetails;
import org.yakovb.ratelimiter.tokenbucket.StoreCleaner;
import org.yakovb.ratelimiter.tokenbucket.TokenBucket;
import org.yakovb.ratelimiter.tokenbucket.TokenBucketStrategy;

@Configuration
@EnableConfigurationProperties(RateLimitConfig.class)
public class BeanConfig {

  private static final Duration DEFAULT_WINDOW = Duration.ofHours(1);
  private static final int DEFAULT_TOKENS = 100;

  @Autowired(required = false)
  private RateLimitConfig rateLimitConfig;

  @Bean
  @ConditionalOnMissingBean
  public RateLimitStrategy rateLimitStrategy(RateLimitDetails rateLimitDetails) {
    return new TokenBucketStrategy(userRequestDataStore(), rateLimitDetails);
  }

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
