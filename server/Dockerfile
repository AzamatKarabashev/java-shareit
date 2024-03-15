FROM amazoncorretto:11
WORKDIR /app
COPY target/*.jar app.jar
ENV TZ="Europe/Moscow"
ENTRYPOINT ["java","-jar","app.jar"]