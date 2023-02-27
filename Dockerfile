FROM maven:3.8.4-openjdk-17-slim as builder
WORKDIR /app/build
ARG USERNAME
ARG TOKEN
COPY ./gradle /app/build/gradle
COPY ./gradlew /app/build/
RUN chmod 777 ./gradlew
RUN ./gradlew build|| return 0
COPY ./common/build.gradle.kts /app/build/common/
COPY ./common/settings.gradle.kts /app/build/common/
COPY ./core/build.gradle.kts /app/build/core/
COPY ./core/settings.gradle.kts /app/build/core/
COPY ./exec/build.gradle.kts /app/build/exec/
COPY ./exec/settings.gradle.kts /app/build/exec/
COPY ./opds/build.gradle.kts /app/build/opds/
COPY ./opds/settings.gradle.kts /app/build/opds/
COPY ./data-storage/build.gradle.kts /app/build/data-storage/
COPY ./data-storage/settings.gradle.kts /app/build/data-storage/
COPY ./build.gradle.kts /app/build/
COPY ./gradle.properties /app/build/
COPY ./settings.gradle.kts /app/build/
RUN ./gradlew build || return 0

COPY ./common /app/build/common
COPY ./core /app/build/core
COPY ./exec /app/build/exec
COPY ./opds /app/build/opds
COPY ./data-storage /app/build/data-storage
COPY ./web /app/build/web
RUN ls .
RUN ./gradlew build

FROM openjdk:17-slim
ENV APP_FILE zombieCore.jar
ENV APP_HOME /app
EXPOSE 8100 8100

COPY --from=builder  /app/build/exec/build/libs/$APP_FILE $APP_HOME/
WORKDIR $APP_HOME
ENTRYPOINT ["sh", "-c"]
CMD ["exec java -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:5005 -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=9010 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.rmi.port=9011 -Djava.rmi.server.hostname=192.168.0.40 -Dcom.sun.management.jmxremote.local.only=false -Dcom.sun.management.jmxremote.ssl=false -jar $APP_FILE"]