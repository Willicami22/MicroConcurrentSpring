FROM eclipse-temurin:25-jdk AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN apt-get update && apt-get install -y maven && mvn package -q

FROM eclipse-temurin:25-jdk
WORKDIR /usrapp/bin
ENV PORT=8080
COPY --from=build /app/target/classes ./classes
COPY --from=build /app/src/main/resources/webroot ./webroot
CMD ["java", "-cp", "./classes", "co.edu.escuelaing.MicroSpringBoot"]
