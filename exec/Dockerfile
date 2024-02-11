FROM openjdk:12-alpine
ENV APP_FILE foreverLibraryCore.jar
ENV APP_HOME /app
EXPOSE 8100 8100
COPY target/$APP_FILE $APP_HOME/
WORKDIR $APP_HOME
ENTRYPOINT ["sh", "-c"]
CMD ["exec java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 -jar $APP_FILE"]