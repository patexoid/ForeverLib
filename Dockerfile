FROM maven:3.6.1-jdk-13-alpine as builder
WORKDIR /app/build
ARG GITHUB_PACKAGE
COPY ./.travis.settings.xml /root/.m2/settings.xml
COPY ./common/pom.xml /app/build/common/
COPY ./core/pom.xml /app/build/core/
COPY ./exec/pom.xml /app/build/exec/
COPY ./opds/pom.xml /app/build/opds/
COPY ./pom.xml /app/build/
RUN mvn package verify
RUN mvn dependency:resolve-plugins

COPY ./common /app/build/common
COPY ./core /app/build/core
COPY ./exec /app/build/exec
COPY ./opds /app/build/opds
COPY ./web /app/build/web
RUN mvn package

FROM openjdk:12-alpine
ENV APP_FILE zombieCore.jar
ENV APP_HOME /app
EXPOSE 8100 8100
COPY --from=builder  /app/build/exec/target/$APP_FILE $APP_HOME/
WORKDIR $APP_HOME
ENTRYPOINT ["sh", "-c"]
CMD ["exec java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 -jar $APP_FILE"]