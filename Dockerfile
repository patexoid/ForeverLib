FROM maven:3.6.3-openjdk-15-slim as builder
WORKDIR /app/build
ARG GITHUB_PACKAGE
COPY ./.travis.settings.xml /root/.m2/settings.xml
COPY ./common/pom.xml /app/build/common/
COPY ./core/pom.xml /app/build/core/
COPY ./exec/pom.xml /app/build/exec/
COPY ./opds/pom.xml /app/build/opds/
COPY ./data-storage/pom.xml /app/build/data-storage/
COPY ./pom.xml /app/build/
RUN mvn package verify
RUN mvn dependency:resolve-plugins

COPY ./common /app/build/common
COPY ./core /app/build/core
COPY ./exec /app/build/exec
COPY ./opds /app/build/opds
COPY ./data-storage /app/build/data-storage
COPY ./web /app/build/web
RUN mvn package

FROM adoptopenjdk/openjdk15:jdk-15.0.1_9-alpine-slim
ENV APP_FILE zombieCore.jar
ENV APP_HOME /app
EXPOSE 8100 8100
COPY --from=builder  /app/build/exec/target/$APP_FILE $APP_HOME/
WORKDIR $APP_HOME
ENTRYPOINT ["sh", "-c"]
CMD ["exec java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=9010 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.rmi.port=9011 -Djava.rmi.server.hostname=192.168.0.40 -Dcom.sun.management.jmxremote.local.only=false -Dcom.sun.management.jmxremote.ssl=false -jar $APP_FILE"]