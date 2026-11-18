package dev.jsamuelap.oikonomiaapi.shared.web.openapi;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;

import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import dev.jsamuelap.oikonomiaapi.shared.security.ratelimit.RateLimitProperties;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.headers.Header;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.responses.ApiResponse;

@Configuration
public class OpenApiErrorsConfig {

  private static final String PROBLEM_JSON = "application/problem+json";
  private static final String SCHEMA_NAME = "ProblemDetail";

  @Bean
  public OpenApiCustomizer problemDetailCustomizer(RateLimitProperties rateLimit) {
    return openApi -> {
      registerSchema(openApi);
      if (rateLimit.enabled()) {
        registerRateLimitHeaders(openApi);
        appendRateLimitDescription(openApi, rateLimit);
      }
      boolean globalSecurity = openApi.getSecurity() != null && !openApi.getSecurity().isEmpty();

      openApi.getPaths().forEach(
        (path, item) -> item.readOperations().forEach(op -> customize(path, op, globalSecurity, rateLimit.enabled())));
    };
  }

  private void registerSchema(OpenAPI openApi) {
    if (openApi.getComponents() == null) {
      openApi.setComponents(new Components());
    }
    Schema<?> problem = new ObjectSchema().addProperty("title", new StringSchema().example("Bad Request"))
      .addProperty("status", new IntegerSchema().example(400)).addProperty("detail", new StringSchema());
    openApi.getComponents().addSchemas(SCHEMA_NAME, problem);
  }

  private void customize(String path, Operation op, boolean globalSecurity, boolean rateLimited) {
    Map<String, ApiResponse> responses = op.getResponses();

    if (hasInput(op)) {
      addIfAbsent(responses, "400", "Bad request");
    }
    if (isSecured(op, globalSecurity)) {
      addIfAbsent(responses, "401", "Unauthenticated or invalid token");
    }
    if (path.contains("{")) {
      addIfAbsent(responses, "404", "Not found");
    }
    if (rateLimited) {
      addIfAbsent(responses, "429", "Too Many Requests");
      addRateLimitHeaders(responses);
    }

    responses.forEach((code, response) -> {
      boolean isError = code.startsWith("4") || code.startsWith("5");
      if (isError && !documentsProblemJson(response)) {
        response.setContent(problemContent());
      }
    });
  }

  private boolean hasInput(Operation op) {
    return op.getRequestBody() != null || (op.getParameters() != null && !op.getParameters().isEmpty());
  }

  private boolean isSecured(Operation op, boolean globalSecurity) {
    return op.getSecurity() != null ? !op.getSecurity().isEmpty() : globalSecurity;
  }

  private void addIfAbsent(Map<String, ApiResponse> responses, String code, String description) {
    responses.computeIfAbsent(code, c -> new ApiResponse().description(description));
  }

  @SuppressWarnings("PMD.LooseCoupling")
  private Content problemContent() {
    return new Content().addMediaType(PROBLEM_JSON,
      new MediaType().schema(new Schema<>().$ref("#/components/schemas/" + SCHEMA_NAME)));
  }

  private boolean documentsProblemJson(ApiResponse response) {
    return response.getContent() != null && response.getContent().containsKey(PROBLEM_JSON);
  }

  private void registerRateLimitHeaders(OpenAPI openApi) {
    Components components = openApi.getComponents();
    components.addHeaders("Retry-After", new Header()
      .description("Segundos que hay que esperar antes de volver a intentarlo.").schema(new IntegerSchema()));
    components.addHeaders("RateLimit-Limit", new Header()
      .description("Capacidad máxima (rafaga) del límite que se aplica a la ruta.").schema(new IntegerSchema()));
    components.addHeaders("RateLimit-Remaining", new Header()
      .description("Peticiones que quedan antes de que empiecen a rechazarse.").schema(new IntegerSchema()));
  }

  private void addRateLimitHeaders(Map<String, ApiResponse> responses) {
    final String TOO_MANY_REQUESTS_CODE = "429";
    responses.forEach((code, response) -> {
      if (code.startsWith("2")) {
        addHeaderRef(response, "RateLimit-Limit");
        addHeaderRef(response, "RateLimit-Remaining");
      } else if (TOO_MANY_REQUESTS_CODE.equals(code)) {
        addHeaderRef(response, "Retry-After");
        addHeaderRef(response, "RateLimit-Limit");
        addHeaderRef(response, "RateLimit-Remaining");
      }
    });
  }

  private void addHeaderRef(ApiResponse response, String name) {
    response.addHeaderObject(name, new Header().$ref("#/components/headers/" + name));
  }

  private void appendRateLimitDescription(OpenAPI openApi, RateLimitProperties props) {
    String section = """

      ## Límites de tasa

      Cada límite admite una ráfaga de hasta la capacidad indicada y se recarga de forma continua.
      Al superarlo, la API responde `429` con la cabecera `Retry-After`.

      | Ruta | Clave | Ráfaga | Recarga |
      |---|---|---|---|
      | `POST /api/v1/auth/login` | IP | %d | %s |
      | `POST /api/v1/auth/signup` | IP | %d | %s |
      | `POST /api/v1/auth/refresh` | IP | %d | %s |
      | Resto de `/api/**` | Usuario (IP sin sesión) | %d | %s |
      """.formatted(props.login().capacity(), describe(props.login()), props.signup().capacity(),
      describe(props.signup()), props.refresh().capacity(), describe(props.refresh()), props.api().capacity(),
      describe(props.api()));

    Info info = openApi.getInfo();
    info.setDescription(Objects.toString(info.getDescription(), "") + section);
  }

  private static String describe(RateLimitProperties.Policy policy) {
    Duration period = policy.refillPeriod();
    String text;
    if (period.toHours() >= 1 && period.toMinutesPart() == 0) {
      text = period.toHours() + " h";
    } else if (period.toMinutes() >= 1 && period.toSecondsPart() == 0) {
      text = period.toMinutes() + " min";
    } else {
      text = period.toSeconds() + " s";
    }
    return policy.refillTokens() + " cada " + text;
  }
}
