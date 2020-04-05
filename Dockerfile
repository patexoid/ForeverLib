FROM maven:3.6.1-jdk-13-alpine as builder
WORKDIR /app/build

COPY ./common/pom.xml /app/build/common/
COPY ./core/pom.xml /app/build/core/
COPY ./exec/pom.xml /app/build/exec/
COPY ./fb2generated/pom.xml /app/build/fb2generated/
COPY ./fuzzySearch/pom.xml /app/build/fuzzySearch/
COPY ./localization/pom.xml /app/build/localization/
COPY ./opds/pom.xml /app/build/opds/
COPY ./pom.xml /app/build/
RUN mvn package verify
RUN mvn dependency:resolve-plugins

COPY ./common /app/build/common
COPY ./core /app/build/core
COPY ./exec /app/build/exec
COPY ./fb2generated /app/build/fb2generated
COPY ./fuzzySearch /app/build/fuzzySearch
COPY ./localization /app/build/localization
COPY ./opds  /app/build/opds
COPY ./web /app/build/web
COPY ./Dockerfile /app/build/
RUN mvn package

FROM openjdk:12-alpine
ENV APP_FILE zombieCore.jar
ENV APP_HOME /app
EXPOSE 8100 8100
COPY --from=builder  /app/build/exec/target/$APP_FILE $APP_HOME/
WORKDIR $APP_HOME
ENTRYPOINT ["sh", "-c"]
CMD ["exec java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 -jar $APP_FILE"]