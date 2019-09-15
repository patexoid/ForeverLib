FROM openjdk:12-alpine
ENV APP_FILE user-service-0.38.jar
ENV APP_HOME /app
EXPOSE 8090 5008
COPY target/$APP_FILE $APP_HOME/
WORKDIR $APP_HOME
ENTRYPOINT ["sh", "-c"]
CMD ["exec java  -Djava.security.egd=file:///dev/./urandom -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5008 -jar $APP_FILE"]