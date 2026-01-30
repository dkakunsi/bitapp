FROM alpine:3.22.2 AS packager

RUN apk --no-cache add openjdk25-jdk openjdk25-jmods
RUN apk --no-cache add freetype binutils

ENV JAVA_MINIMAL="/opt/java-minimal"
ENV MY_USER="bitapp"

# build minimal JRE
RUN /usr/lib/jvm/java-25-openjdk/bin/jlink \
  --verbose \
  --add-modules java.base,java.desktop,java.management,java.logging,java.security.sasl,jdk.crypto.cryptoki,java.naming,java.instrument,java.sql \
  --compress 2 --strip-debug --no-header-files --no-man-pages \
  --release-info="add:IMPLEMENTOR=radistao:IMPLEMENTOR_VERSION=radistao_JRE" \
  --output "$JAVA_MINIMAL"

RUN addgroup --gid 1001 -S "$MY_USER" && \
    adduser -G "$MY_USER" --shell /bin/false --disabled-password -H --uid 1001 "$MY_USER" && \
    mkdir -p "/var/log/$MY_USER" && \
    chown "$MY_USER":"$MY_USER" "/var/log/$MY_USER"

## APP
FROM alpine:3.22.2

ENV JAVA_HOME=/opt/java-minimal
ENV PATH="$PATH:$JAVA_HOME/bin"

COPY --from=packager "$JAVA_HOME" "$JAVA_HOME"

ENV APP_HOME=/opt/bitapp
WORKDIR "$APP_HOME"

COPY ./maven/app.jar .

RUN set -x && chmod 755 "$APP_HOME"

# RUN keytool -importcert -trustcacerts -noprompt -alias bitapp_idm_cert -keystore "$JAVA_HOME/lib/security/cacerts" -storepass changeit -file server.cer

ENV JAVA_TOOL_OPTIONS=
ENV OTEL_TRACES_EXPORTER=otlp
ENV OTEL_METRICS_EXPORTER=otlp
ENV OTEL_LOGS_EXPORTER=logging
ENV OTEL_SERVICE_NAME=money
ENV OTEL_EXPORTER_OTLP_ENDPOINT=

EXPOSE 2000
USER "$MY_USER"

ENTRYPOINT exec java -Duser.timezone=Asia/Makassar -jar app.jar
