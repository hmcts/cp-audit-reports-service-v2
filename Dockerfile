# ---- Base image (default fallback) ----
ARG BASE_IMAGE
FROM ${BASE_IMAGE:-eclipse-temurin:25-jre}

# minimal runtime tooling for healthcheck
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

# run as non-root
RUN groupadd -r app && useradd -r -g app app
WORKDIR /app

# copy all jars (bootJar + plain). We'll run the non-plain jar.
COPY build/libs/*.jar /app/

EXPOSE ${SERVER_PORT:-8082}
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+AlwaysActAsServerClassMachine"

HEALTHCHECK --interval=10s --timeout=3s --start-period=20s --retries=15 \
  CMD curl -fsS http://localhost:${SERVER_PORT:-8082}/actuator/health | grep -q '"status":"UP"' || exit 1

USER app
# pick the Boot fat jar (exclude '-plain.jar')
ENTRYPOINT ["sh","-c","exec java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar $(ls /app/*.jar | grep -v 'plain' | head -n1)"]
