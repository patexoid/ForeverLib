FROM openjdk:12-alpine
ENV APP_FILE zombieCore.jar
ENV APP_HOME /app
EXPOSE 8088 5005
COPY target/$APP_FILE $APP_HOME/
WORKDIR $APP_HOME
ENTRYPOINT ["sh", "-c"]
CMD ["exec java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 -Dspring.profiles.active=feignStorage -jar $APP_FILE"]