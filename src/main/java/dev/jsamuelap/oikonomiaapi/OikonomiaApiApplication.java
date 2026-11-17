package dev.jsamuelap.oikonomiaapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import dev.jsamuelap.oikonomiaapi.shared.security.config.CorsProperties;
import dev.jsamuelap.oikonomiaapi.shared.security.config.SecurityProperties;
import dev.jsamuelap.oikonomiaapi.shared.security.jwt.JwtProperties;
import dev.jsamuelap.oikonomiaapi.shared.security.ratelimit.RateLimitProperties;

@SpringBootApplication
@EnableConfigurationProperties({CorsProperties.class, SecurityProperties.class, JwtProperties.class,
  RateLimitProperties.class})
public class OikonomiaApiApplication {

  public static void main(String[] args) {
    SpringApplication.run(OikonomiaApiApplication.class, args);
  }

}
