package dev.jsamuelap.oikonomiaapi.shared.web.openapi;

import java.util.Map;

import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
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
  public OpenApiCustomizer problemDetailCustomizer() {
    return openApi -> {
      registerSchema(openApi);
      boolean globalSecurity = openApi.getSecurity() != null && !openApi.getSecurity().isEmpty();

      openApi.getPaths()
        .forEach((path, item) -> item.readOperations().forEach(op -> customize(path, op, globalSecurity)));
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

  private void customize(String path, Operation op, boolean globalSecurity) {
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
}
