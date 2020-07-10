FROM adoptopenjdk/openjdk11:latest
WORKDIR /app
COPY . .
RUN ./gradlew assemble
ENTRYPOINT ["./gradlew"]
CMD ["common:features"]
