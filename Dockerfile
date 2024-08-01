FROM openjdk:17-alpine

VOLUME /tmp

COPY target/odm-platform-adapter-executor-gitlab-*.jar ./application.jar

ARG SPRING_PROFILES_ACTIVE=docker
ARG JAVA_OPTS
ARG SPRING_PORT=9004
ARG SPRING_PROPS
ARG PARAMS_SERVICE_ADDRESS=http://localhost:8004
ARG PARAMS_SERVICE_UUID=client123
ARG DATABASE_URL=jdbc:postgresql://localhost:5432/odmpdb
ARG DATABASE_USERNAME=usr
ARG DATABASE_PASSWORD=pwd
ARG FLYWAY_SCHEMA=ODMEXECUTOR
ARG FLYWAY_SCRIPTS_DIR=postgresql
ARG H2_CONSOLE_ENABLED=false
ARG H2_CONSOLE_PATH=h2-console

ENV SPRING_PROFILES_ACTIVE ${SPRING_PROFILES_ACTIVE}
ENV JAVA_OPTS ${JAVA_OPTS}
ENV SPRING_PORT ${SPRING_PORT}
ENV SPRING_PROPS ${SPRING_PROPS}
ENV PARAMS_SERVICE_ADDRESS ${PARAMS_SERVICE_ADDRESS}
ENV PARAMS_SERVICE_UUID ${PARAMS_SERVICE_UUID}
ENV DATABASE_URL ${DATABASE_URL}
ENV DATABASE_USERNAME ${DATABASE_USERNAME}
ENV DATABASE_PASSWORD ${DATABASE_PASSWORD}
ENV FLYWAY_SCHEMA ${FLYWAY_SCHEMA}
ENV FLYWAY_SCRIPTS_DIR ${FLYWAY_SCRIPTS_DIR}
ENV H2_CONSOLE_ENABLED ${H2_CONSOLE_ENABLED}
ENV H2_CONSOLE_PATH ${H2_CONSOLE_PATH}

EXPOSE $SPRING_PORT

ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS  -Dspring.profiles.active=$SPRING_PROFILES_ACTIVE $SPRING_PROPS -jar ./application.jar" ]