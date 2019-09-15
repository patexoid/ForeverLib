FROM openjdk:12-alpine
ENV APP_FILE gateway-service-1.0-SNAPSHOT.jar
ENV APP_HOME /app
EXPOSE 8060 5010
COPY target/$APP_FILE $APP_HOME/
WORKDIR $APP_HOME
ENTRYPOINT ["sh", "-c"]
CMD ["exec java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5010 -jar $APP_FILE"]