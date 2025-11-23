#!/bin/bash

echo "Starting Mock Employee API..."
./gradlew server:bootRun > server.log 2>&1 &
SERVER_PID=$!

echo "Waiting for server to start..."
# Wait for server to be ready by checking the port
while ! curl -s http://localhost:8112/api/v1/employee > /dev/null 2>&1; do
    sleep 1
    echo "Still waiting..."
done

echo "Server is ready! Running end-to-end tests..."
./gradlew test --tests "com.reliaquest.integration.EndToEndTest"

echo "Stopping Mock Employee API..."
kill $SERVER_PID
wait $SERVER_PID 2>/dev/null

echo "E2E tests completed!"
