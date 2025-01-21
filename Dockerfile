FROM maven:3.8-openjdk-17-slim as builder

WORKDIR /builder

COPY src ./src

COPY pom.xml .

RUN mvn clean package -DskipTests

FROM openjdk:17-alpine as app

WORKDIR /var/www/src/

COPY --from=builder /builder/target/castlemania-2.jar cas-app.jar

CMD ["java", "-jar", "cas-app.jar"]