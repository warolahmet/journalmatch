FROM eclipse-temurin:17-jdk

WORKDIR /app

RUN apt-get update && apt-get install -y python3 python3-pip python3-venv && rm -rf /var/lib/apt/lists/*

COPY . .

RUN chmod +x mvnw
RUN chmod +x start.sh

RUN python3 -m venv /opt/venv

RUN echo "Installing fastapi and uvicorn..." && \
    /opt/venv/bin/pip install --no-cache-dir fastapi uvicorn

RUN echo "Installing sentence-transformers..." && \
    /opt/venv/bin/pip install --no-cache-dir --progress-bar off sentence-transformers

RUN echo "Building Spring Boot app..." && \
    ./mvnw clean package -DskipTests

EXPOSE 8080

CMD ["sh", "./start.sh"]