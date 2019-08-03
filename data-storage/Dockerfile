FROM openjdk:12-alpine
ENV APP_FILE dataStorage-0.38.jar
ENV APP_HOME /app
EXPOSE 8090 5006
COPY target/$APP_FILE $APP_HOME/
WORKDIR $APP_HOME
ENTRYPOINT ["sh", "-c"]
CMD ["exec java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5006 -jar $APP_FILE"]