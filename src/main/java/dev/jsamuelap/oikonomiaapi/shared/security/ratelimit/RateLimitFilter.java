package dev.jsamuelap.oikonomiaapi.shared.security.ratelimit;

import java.io.IOException;
import java.time.Duration;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import dev.jsamuelap.oikonomiaapi.shared.security.jwt.AuthenticatedPrincipal;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {
  private final RateLimitProperties properties;
  private final JacksonJsonHttpMessageConverter converter;

  private final Cache<String, Bucket> buckets = Caffeine.newBuilder().maximumSize(100_000)
    .expireAfterAccess(Duration.ofHours(2)).build();

  private final PathPatternRequestMatcher.Builder paths = PathPatternRequestMatcher.withDefaults();
  private final RequestMatcher login = paths.matcher(HttpMethod.POST, "/api/v1/auth/login");
  private final RequestMatcher signup = paths.matcher(HttpMethod.POST, "/api/v1/auth/signup");
  private final RequestMatcher refresh = paths.matcher(HttpMethod.POST, "/api/v1/auth/refresh");
  private final RequestMatcher api = paths.matcher("/api/**");

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
    throws ServletException, IOException {
    Optional<RateLimitPolicy> resolve = policyFor(request);
    if (resolve.isEmpty()) {
      filterChain.doFilter(request, response);
      return;
    }

    RateLimitPolicy policy = resolve.get();
    RateLimitProperties.Policy limits = properties.limitsFor(policy);
    Bucket bucket = buckets.get(keyFor(policy, request), key -> newBucket(limits));
    ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

    response.setHeader("RateLimit-Limit", String.valueOf(limits.capacity()));
    response.setHeader("RateLimit-Remaining", String.valueOf(probe.getRemainingTokens()));

    if (probe.isConsumed()) {
      filterChain.doFilter(request, response);
      return;
    }
    reject(response, probe);
  }

  private Optional<RateLimitPolicy> policyFor(HttpServletRequest request) {
    if (HttpMethod.OPTIONS.matches(request.getMethod())) {
      // no consumir tokens en los preflight de CORS
      return Optional.empty();
    }
    if (login.matches(request)) {
      return Optional.of(RateLimitPolicy.LOGIN);
    }
    if (signup.matches(request)) {
      return Optional.of(RateLimitPolicy.SIGNUP);
    }
    if (refresh.matches(request)) {
      return Optional.of(RateLimitPolicy.REFRESH);
    }
    return api.matches(request) ? Optional.of(RateLimitPolicy.API) : Optional.empty();
  }

  private String keyFor(RateLimitPolicy policy, HttpServletRequest request) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (policy == RateLimitPolicy.API && authentication != null
      && authentication.getPrincipal() instanceof AuthenticatedPrincipal principal) {
      return "api:user:" + principal.userId();
    }
    return policy.name().toLowerCase(Locale.ROOT) + ":ip" + request.getRemoteAddr();
  }

  private static Bucket newBucket(RateLimitProperties.Policy limits) {
    return Bucket.builder()
      .addLimit(limit -> limit.capacity(limits.capacity()).refillGreedy(limits.refillTokens(), limits.refillPeriod()))
      .build();
  }

  private void reject(HttpServletResponse response, ConsumptionProbe probe) throws IOException {
    long retryAfterSeconds = TimeUnit.NANOSECONDS.toSeconds(probe.getNanosToWaitForRefill()) + 1;
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.TOO_MANY_REQUESTS,
      "Demasiadas solicitudes. Intenta de nuevo más tarde.");
    response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
    response.setHeader(HttpHeaders.RETRY_AFTER, String.valueOf(retryAfterSeconds));
    response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
    converter.write(problem, null, new ServletServerHttpResponse(response));
  }
}
