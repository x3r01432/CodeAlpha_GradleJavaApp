FROM eclipse-temurin:21.0.5_11-jre-alpine
RUN addgroup -S app && adduser -S app -G app
WORKDIR /app
COPY app/build/libs/todo-api-*-all.jar /app/app.jar
USER app
EXPOSE 8080
HEALTHCHECK --interval=10s --timeout=3s CMD wget -qO- http://127.0.0.1:8080/health || exit 1
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
