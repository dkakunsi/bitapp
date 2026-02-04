FROM eclipse-temurin:25.0.1_8-jre-alpine-3.23

ENV APP_HOME=/opt/bitapp
WORKDIR "$APP_HOME"

COPY ./maven/app.jar .

RUN set -x && chmod 755 "$APP_HOME"

# RUN keytool -importcert -trustcacerts -noprompt -alias bitapp_idm_cert -keystore "$JAVA_HOME/lib/security/cacerts" -storepass changeit -file server.cer

ENV JAVA_TOOL_OPTIONS=
ENV OTEL_TRACES_EXPORTER=otlp
ENV OTEL_METRICS_EXPORTER=otlp
ENV OTEL_LOGS_EXPORTER=logging
ENV OTEL_SERVICE_NAME=bitapp
ENV OTEL_EXPORTER_OTLP_ENDPOINT=

EXPOSE 2000

ENTRYPOINT exec java -Duser.timezone=Asia/Makassar -jar app.jar
