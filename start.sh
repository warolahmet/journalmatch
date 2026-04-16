#!/bin/sh

echo "Starting local embedding API..."
/opt/venv/bin/python -m uvicorn scripts.embedding_local_api:app --host 127.0.0.1 --port 8001 &

echo "Waiting for embedding API to warm up..."
sleep 10

echo "Starting Spring Boot app..."
java -jar target/*.jar
