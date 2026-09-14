# syntax=docker/dockerfile:1

# ============================================================================
# Stage 1: Build
# Uses the same JDK 26 base as the reference project, but as a proper
# multi-stage build: pom.xml is copied and dependencies resolved *before* the
# source is copied, so Docker layer caching avoids re-downloading every
# dependency on every rebuild (only source changes bust that cache).
# ============================================================================
FROM eclipse-temurin:26-jdk AS build

WORKDIR /app

RUN apt-get update && apt-get install -y --no-install-recommends maven \
    && rm -rf /var/lib/apt/lists/*

COPY pom.xml .
RUN mvn -B dependency:go-offline || true

COPY src ./src
RUN mvn -B clean package -DskipTests

# ============================================================================
# Stage 2: Runtime
# Same JDK 26 base (kept identical to the build stage to guarantee the image
# tag exists and behaves identically) but only the built jar is copied in -
# no Maven, no source, no .git, no dev dependencies ship in the final image.
# ============================================================================
FROM eclipse-temurin:26-jdk AS runtime

WORKDIR /app

COPY --from=build /app/target/contextenglish-0.0.1-SNAPSHOT.jar app.jar

# Render injects $PORT at runtime; the app reads it via server.port=${PORT:8080}
# in application.properties. EXPOSE here is informational for local `docker run`.
EXPOSE 8080

# Conservative JVM memory flags suited to Render's smaller instance tiers.
# Increase -Xmx if you upgrade to a larger plan.
ENTRYPOINT ["sh", "-c", "java -Xmx400m -XX:+UseSerialGC -jar app.jar"]
