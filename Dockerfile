#
# Build
#

FROM gradle:jdk11 AS BUILDER

COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src

RUN gradle --exclude-task test --no-daemon --console plain --info clean assemble

#
# Run
#

FROM openjdk:11-jre-slim

ENV APP="ravine" \
    APP_HOME="/app" \
    UID="1111" \
    GID="1111"

RUN mkdir -p ${APP_HOME}
COPY --from=BUILDER /home/gradle/src/build/libs/${APP}.jar ${APP_HOME}/
WORKDIR ${APP_HOME}

RUN addgroup --quiet --gid ${GID} ${APP}
RUN adduser --home ${APP_HOME} --uid ${UID} --gid ${GID} \
        --disabled-password --disabled-login --no-create-home --quiet ${APP}
USER ${APP}

ENTRYPOINT ["java", "-jar", "/app/ravine.jar"]
