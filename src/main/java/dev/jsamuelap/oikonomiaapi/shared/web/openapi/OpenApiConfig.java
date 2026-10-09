package dev.jsamuelap.oikonomiaapi.shared.web.openapi;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

@Configuration
// spotless:off
@OpenAPIDefinition(
  info = @Info(
    title = "Oikonomía API",
    version = "1.0.0",
    description = "REST API for Oikonomía personal finance management application.",
    contact = @Contact(name = "JSamuel", email = "sp4619168@gmail.com", url = "https://jsamuelap.github.io"),
    license = @License(name = "MIT", url = "https://opensource.org/licenses/MIT")
  ),
  security = @SecurityRequirement(name = "bearerAuth")
)
// spotless:on
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
public class OpenApiConfig {}
