package dev.jsamuelap.oikonomiaapi.shared.security.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security")
public record SecurityProperties(boolean cookieSecure) {
}
