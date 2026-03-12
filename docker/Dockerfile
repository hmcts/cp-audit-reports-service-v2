# ---- Base image (default fallback) ----
FROM docker.io/library/eclipse-temurin:25-jre-alpine

# run as non-root ... group and user "app"
RUN addgroup -S app \
    && adduser -S app -G app \
    && mkdir /app \
    && chown -R app:app /app

WORKDIR /app

# copy all jars (bootJar + plain). We'll run the non-plain jar.
COPY build/libs/*.jar /app/

# JVM/system overrides to *force* tracing/exporter off
ENV JAVA_OPTS="\
-XX:MaxRAMPercentage=75 \
-XX:+AlwaysActAsServerClassMachine \
-Dmanagement.tracing.enabled=false \
-Dmanagement.otlp.tracing.export.enabled=false \
-Dspring.autoconfigure.exclude=org.springframework.boot.actuate.autoconfigure.tracing.TracingAutoConfiguration,org.springframework.boot.actuate.autoconfigure.tracing.OpenTelemetryTracingAutoConfiguration,org.springframework.boot.actuate.autoconfigure.tracing.otlp.OtlpTracingAutoConfiguration,org.springframework.boot.actuate.autoconfigure.tracing.otlp.OtlpAutoConfiguration \
"

USER app
ENTRYPOINT ["sh","-c","exec java $JAVA_OPTS -Djava.cd security.egd=file:/dev/./urandom -jar $(ls /app/*.jar | grep -v 'plain' | head -n1)"]