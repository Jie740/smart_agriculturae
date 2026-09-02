FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /build

COPY pom.xml .
RUN mvn -B -Dmaven.test.skip=true dependency:go-offline

COPY src ./src
RUN mvn -B -Dmaven.test.skip=true clean package dependency:copy-dependencies \
    -DincludeScope=runtime \
    -DoutputDirectory=target/dependency

FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=build /build/target/*.jar app.jar
COPY --from=build /build/target/dependency ./dependency

ENV SPRING_PROFILES_ACTIVE=prod

EXPOSE 8080

ENTRYPOINT ["java", "-cp", "app.jar:dependency/*", "com.clj.AgriculturalSystemApplication"]
