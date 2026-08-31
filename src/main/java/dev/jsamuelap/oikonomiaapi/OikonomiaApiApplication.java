package dev.jsamuelap.oikonomiaapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import dev.jsamuelap.oikonomiaapi.shared.security.config.SecurityProperties;
import dev.jsamuelap.oikonomiaapi.shared.security.jwt.JwtProperties;

@SpringBootApplication
@EnableConfigurationProperties({SecurityProperties.class, JwtProperties.class})
public class OikonomiaApiApplication {

  public static void main(String[] args) {
    SpringApplication.run(OikonomiaApiApplication.class, args);
  }

}
