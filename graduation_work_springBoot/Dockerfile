
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /build

# 配置 Maven 国内镜像
COPY settings.xml /root/.m2/settings.xml

# 先复制依赖配置，利用 Docker 缓存
COPY pom.xml .

# 下载依赖
RUN mvn -s /root/.m2/settings.xml \
    -B -Dmaven.test.skip=true \
    dependency:go-offline

# 复制源代码
COPY src ./src

# 编译打包
RUN mvn -s /root/.m2/settings.xml \
    -B -Dmaven.test.skip=true \
    clean package dependency:copy-dependencies \
    -DincludeScope=runtime \
    -DoutputDirectory=target/dependency

FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=build /build/target/*.jar app.jar
COPY --from=build /build/target/dependency ./dependency

ENV SPRING_PROFILES_ACTIVE=prod

EXPOSE 8080

ENTRYPOINT ["java", "-cp", "app.jar:dependency/*", "com.clj.AgriculturalSystemApplication"]
