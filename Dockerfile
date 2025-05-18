FROM eclipse-temurin:11-jdk AS build
WORKDIR /app

# Skopiuj wszystko z katalogu backend
COPY . .

# Upewnij się, że wrapper jest wykonywalny, a następnie zbuduj
RUN chmod +x ./gradlew && \
    ./gradlew build -x test --no-daemon

# Stage 2: runtime na Java 11
FROM eclipse-temurin:11-jre
WORKDIR /app

# Skopiuj zbudowany jar
COPY --from=build /app/build/libs/*.jar app.jar

# Otwórz port
EXPOSE 8080

# Uruchomienie aplikacji
ENTRYPOINT ["java","-jar","app.jar"]