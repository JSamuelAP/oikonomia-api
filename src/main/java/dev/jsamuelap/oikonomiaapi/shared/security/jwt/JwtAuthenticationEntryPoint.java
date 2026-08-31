package dev.jsamuelap.oikonomiaapi.shared.security.jwt;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
  private final JacksonJsonHttpMessageConverter converter = new JacksonJsonHttpMessageConverter();

  @Override
  public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
    throws IOException, ServletException {
    // Se maneja aquí y no en el global handler ya que esta excepción ocurre antes
    // de entrar al controller
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED,
      "No autenticado o token inválido");
    problem.setTitle("Error de autorización");

    response.setStatus(HttpStatus.UNAUTHORIZED.value());
    response.setContentType("application/problem+json");
    converter.write(problem, null, new org.springframework.http.server.ServletServerHttpResponse(response));
  }
}
