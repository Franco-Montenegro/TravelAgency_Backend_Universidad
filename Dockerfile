FROM eclipse-temurin:17-jre-alpine
ARG JAR_FILE=target/travel-agency-backend.jar
COPY ${JAR_FILE} travel-agency-backend.jar
ENTRYPOINT ["java","-jar","/travel-agency-backend.jar"]