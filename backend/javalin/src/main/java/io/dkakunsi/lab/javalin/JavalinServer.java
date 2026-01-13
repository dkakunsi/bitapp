package io.dkakunsi.lab.javalin;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.dkakunsi.bitapp.common.Logger;
import io.dkakunsi.bitapp.common.SystemLogger;
import io.javalin.Javalin;
import io.javalin.config.JavalinConfig;
import io.javalin.http.ExceptionHandler;
import io.javalin.json.JavalinJackson;

/**
 * Javalin implementation of web service.
 *
 * @author dkakunsi
 */
public final class JavalinServer {

  private static final Logger LOGGER = SystemLogger.getLogger(JavalinServer.class);

  private Javalin app;

  private int port;

  @SuppressWarnings("rawtypes")
  private List<JavalinEndpoint> endpoints;

  private JavalinServer(int port) {
    this.port = port;
    endpoints = new ArrayList<>();
  }

  public static JavalinServer of(int port) {
    return new JavalinServer(port);
  }

  public static JavalinServer of() {
    return of(8080);
  }

  public <S, T> JavalinServer addEndpoint(JavalinEndpoint<S, T> endpoint) {
    endpoints.add(endpoint);
    return this;
  }

  public JavalinServer start() {
    app = Javalin.create(getConfigurer()).start(port);

    initEndpoint();
    initExceptionHandling();

    return this;
  }

  public void stop() {
    app.stop();
  }

  private Consumer<JavalinConfig> getConfigurer() {
    return config -> {
      config.http.maxRequestSize = 1000000;
      config.jsonMapper(new JavalinJackson(createObjectMapper(), true));
    };
  }

  private ObjectMapper createObjectMapper() {
    var mapper = new ObjectMapper();
    mapper.registerModule(new JavaTimeModule());
    mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    return mapper;
  }

  private void initEndpoint() {
    endpoints.forEach(e -> {
      app.addHttpHandler(e.getHandlerType(), e.getPath(), e.getHandler());
    });
  }

  private void initExceptionHandling() {
    app.exception(IllegalArgumentException.class, exceptionHandler(400));
    app.exception(RuntimeException.class, exceptionHandler(500));
    app.exception(Exception.class, exceptionHandler(500));
  }

  private static ExceptionHandler<Exception> exceptionHandler(int statusCode) {
    return (ex, ctx) -> {
      LOGGER.error("Cannot process request. Reason: {}", ex, ex.getMessage());
      ctx.status(statusCode).contentType("text/plain").result(ex.getMessage());
    };
  }
}
