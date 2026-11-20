package dev.jsamuelap.oikonomiaapi.shared.security.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import dev.jsamuelap.oikonomiaapi.shared.security.jwt.JwtAuthenticationEntryPoint;
import dev.jsamuelap.oikonomiaapi.shared.security.jwt.JwtAuthenticationFilter;
import dev.jsamuelap.oikonomiaapi.shared.security.jwt.JwtService;
import dev.jsamuelap.oikonomiaapi.shared.security.ratelimit.RateLimitFilter;
import dev.jsamuelap.oikonomiaapi.shared.security.ratelimit.RateLimitProperties;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
  private final JwtService jwtService;
  private final RateLimitProperties rateLimitProperties;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationEntryPoint entryPoint,
    @Value("${springdoc.api-docs.enabled:true}") boolean apiDocsEnabled,
    @Value("${springdoc.swagger-ui.enabled:true}") boolean swaggerUiEnabled) throws Exception {
    // CSRF deshabilitado: API stateless autenticada con el header Bearer y refresh
    // token en cookie SameSite=Strict, así que el navegador no envía credenciales
    // cross-site. Ver la sección Seguridad del README para más detalles. Reactivar
    // si se añade autenticación por cookie/sesión en otros endpoints.
    http.cors(Customizer.withDefaults()).csrf(AbstractHttpConfigurer::disable)
      .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
      .exceptionHandling(exception -> exception.authenticationEntryPoint(entryPoint))
      .authorizeHttpRequests(authorize -> {
        if (apiDocsEnabled) {
          authorize.requestMatchers("/api-docs.yaml", "/api-docs/**").permitAll();
        }
        if (swaggerUiEnabled) {
          authorize.requestMatchers("/swagger-ui.html", "/swagger-ui/**").permitAll();
        }

        authorize.requestMatchers("/api/v1/auth/logout").authenticated().requestMatchers("/api/v1/auth/**").permitAll()
          .anyRequest().authenticated();
      }).addFilterBefore(new JwtAuthenticationFilter(jwtService), UsernamePasswordAuthenticationFilter.class);

    if (rateLimitProperties.enabled()) {
      http.addFilterAfter(new RateLimitFilter(rateLimitProperties, new JacksonJsonHttpMessageConverter()),
        JwtAuthenticationFilter.class);
    }

    return http.build();
  }
}
