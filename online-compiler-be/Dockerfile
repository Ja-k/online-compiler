# --- Build stage ---
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# Resolve dependencies before copying source, so `docker build` can cache this
# (expensive) layer across rebuilds that only change application code.
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src src
RUN mvn -B -q clean package -DskipTests

# --- Runtime stage ---
# eclipse-temurin's *-alpine variants are amd64-only, so the Ubuntu-based
# "jammy" variant is used instead for arm64 (Apple Silicon) support.
FROM eclipse-temurin:17-jre-jammy AS runtime

# This service shells out to the `docker` CLI itself (see DockerCommandRunner)
# to compile/run untrusted submissions in short-lived containers. Rather than
# running a nested Docker-in-Docker engine, it talks to the HOST's daemon
# through /var/run/docker.sock, mounted in at `docker run` time:
#
#   docker run -d --name online-compiler-be \
#     -v /var/run/docker.sock:/var/run/docker.sock \
#     -p 9191:9191 online-compiler-be
#
# That "sibling containers" pattern only needs the lightweight CLI client
# here, not the full engine - hence `docker.io` (Ubuntu's package containing
# the docker CLI), with recommended/daemon-related extras skipped.
#
# Note: this container therefore needs to run as root (the default here) so
# it can access the socket regardless of the host's docker group GID, which
# effectively grants it root-equivalent control over the host's Docker
# daemon. That's an accepted tradeoff for local/self-hosted dev, but is worth
# knowing before deploying this anywhere multi-tenant.
RUN apt-get update \
	&& apt-get install -y --no-install-recommends docker.io \
	&& rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

EXPOSE 9191

ENTRYPOINT ["java", "-jar", "app.jar"]
