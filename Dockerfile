# ---- Base image (default fallback) ----
ARG BASE_IMAGE
FROM ${BASE_IMAGE:-docker.io/library/eclipse-temurin:25-jre-alpine}

# run as non-root ... group and user "app"
RUN addgroup -S app && adduser -S app -G app
WORKDIR /app

# ---- Application files ----
COPY docker/* /app/
COPY build/libs/*.jar /app/
COPY lib/applicationinsights.json /app/

# Not sure this does anything useful we can drop once we sort certificates
RUN test -n "$JAVA_HOME" \
 && test -f "$JAVA_HOME/lib/security/cacerts" \
 && chmod 777 "$JAVA_HOME/lib/security/cacerts"

USER app
ENTRYPOINT ["/bin/sh","./startup.sh"]