package dev.jsamuelap.oikonomiaapi.shared.security.jwt;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
  private static final String BEARER_PREFIX = "Bearer ";
  private final JwtService jwtService;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
    throws ServletException, IOException {
    String header = request.getHeader("Authorization");

    if (header == null || !header.startsWith(BEARER_PREFIX)) {
      filterChain.doFilter(request, response);
      return;
    }

    String token = header.substring(BEARER_PREFIX.length());

    try {
      Claims claims = jwtService.parse(token);

      if (!"access".equals(claims.get("type", String.class))) {
        filterChain.doFilter(request, response);
        return;
      }

      UUID userId = UUID.fromString(claims.getSubject());
      String email = claims.get("email", String.class);
      AuthenticatedPrincipal principal = new AuthenticatedPrincipal(userId, email);

      var authentication = new UsernamePasswordAuthenticationToken(principal, null, List.of());

      SecurityContextHolder.getContext().setAuthentication(authentication);
    } catch (JwtException | IllegalArgumentException e) {
      SecurityContextHolder.clearContext();
    }

    filterChain.doFilter(request, response);
  }
}
