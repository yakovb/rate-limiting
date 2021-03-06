package org.yakovb.ratelimiter.servlet;

import java.io.IOException;
import java.time.Instant;
import java.util.Optional;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.yakovb.ratelimiter.genericimpl.RequestImpl;
import org.yakovb.ratelimiter.model.RateLimitResult;
import org.yakovb.ratelimiter.model.RateLimitStrategy;

/**
 * Convenience class provided to library users so they don't have to intercept HTTP requests before applying their
 * rate limiting strategy. This servlet filter will block or allow requests based on the results of applying the strategy.
 */
public class RateLimitFilter implements Filter {

  private static final String USER_ID = "user-id";

  private final RateLimitStrategy rateLimitStrategy;

  public RateLimitFilter(RateLimitStrategy rateLimitStrategy) {
    this.rateLimitStrategy = rateLimitStrategy;
  }

  @Override
  public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
      throws IOException, ServletException {

    HttpServletRequest request = (HttpServletRequest) servletRequest;
    String userId = request.getHeader(USER_ID);

    // User-id header is required
    if (userId == null) {
      HttpServletResponse response = toHttpResponse(servletResponse);
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.getWriter().println("Header 'user-id' is required");
      return;
    }

    // Apply the strategy
    Optional<RateLimitResult> blockResult = rateLimitStrategy.apply(new RequestImpl(userId, Instant.now()));
    if (blockResult.isPresent()) {
      // Need to block
      RateLimitResult block = blockResult.get();
      HttpServletResponse response = toHttpResponse(servletResponse);
      response.setStatus(block.getHttpCode());
      response.getWriter().println(block.getMessage());
      return;
    }

    // Allow the request
    filterChain.doFilter(servletRequest, servletResponse);
  }

  private static HttpServletResponse toHttpResponse(ServletResponse servletResponse) {
    HttpServletResponse response = (HttpServletResponse) servletResponse;
    response.reset();
    response.setContentType("text/plain");
    return response;
  }
}
