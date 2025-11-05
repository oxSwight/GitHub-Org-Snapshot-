# ---------- Build Stage ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

COPY pom.xml ./
RUN mvn -B -q -DskipTests -Dmaven.wagon.http.retryHandler.count=5 dependency:resolve

COPY src ./src
RUN mvn -B -q -DskipTests -Dmaven.wagon.http.retryHandler.count=5 package

# ---------- Runtime Stage ----------
FROM eclipse-temurin:17-jre
WORKDIR /opt/app

ENV JAVA_OPTS="-Xms256m -Xmx512m"
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
