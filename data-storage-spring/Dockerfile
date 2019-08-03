FROM openjdk:12-alpine
ENV APP_FILE data-storage-spring-0.38.jar
ENV APP_HOME /app
EXPOSE 8090 5007
COPY target/$APP_FILE $APP_HOME/
WORKDIR $APP_HOME
ENTRYPOINT ["sh", "-c"]
CMD ["exec java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5007 -jar $APP_FILE"]