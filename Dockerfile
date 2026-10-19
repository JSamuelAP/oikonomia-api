FROM eclipse-temurin:25-jdk AS builder
WORKDIR /build

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
COPY src/ src/

RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw -B package -DskipTests \
    -Dspotless.check.skip=true -Dcheckstyle.skip=true -Dpmd.skip=true

RUN cp target/*.jar application.jar \
    && java -Djarmode=tools -jar application.jar extract --layers --destination extracted

FROM eclipse-temurin:25-jre
WORKDIR /app

RUN groupadd --system app && useradd --system --gid app --no-create-home app

COPY --from=builder /build/extracted/dependencies/ ./
COPY --from=builder /build/extracted/spring-boot-loader/ ./
COPY --from=builder /build/extracted/snapshot-dependencies/ ./
COPY --from=builder /build/extracted/application/ ./

USER app
ENV SERVER_PORT=8080
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "application.jar"]
