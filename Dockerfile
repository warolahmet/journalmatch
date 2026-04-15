FROM eclipse-temurin:17-jdk

WORKDIR /app

RUN apt-get update && apt-get install -y python3 python3-pip python3-venv && rm -rf /var/lib/apt/lists/*

COPY . .

RUN chmod +x mvnw

RUN python3 -m venv /opt/venv
RUN /opt/venv/bin/pip install --no-cache-dir sentence-transformers

RUN ./mvnw clean package -DskipTests

EXPOSE 8080

CMD ["sh", "-c", "java -jar target/*.jar"]