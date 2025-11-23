#!/bin/bash

echo "💥 EXTREME Load Test - Forcing Rate Limits and Retries..."

# Test: Hammer individual employee lookups (bypasses cache)
echo "Hammering individual employee lookups (bypasses getAllEmployees cache)..."

# Get some employee IDs first
EMPLOYEE_IDS=$(curl -s "http://localhost:8111/api/v1/employee" | jq -r '.[0:10] | .[].id')

echo "Got employee IDs, starting extreme load..."

# Launch 500 concurrent individual employee requests
count=0
for id in $EMPLOYEE_IDS; do
    for i in {1..50}; do
        curl -s "http://localhost:8111/api/v1/employee/$id" > /dev/null &
        count=$((count + 1))
        if [ $((count % 50)) -eq 0 ]; then
            echo "Launched $count individual employee requests..."
        fi
    done
done

wait
echo "✅ $count individual employee requests completed"

echo "💥 This should have triggered rate limiting and Resilience4j retries!"
echo "Check both terminals for:"
echo "  - Server: Rate limiting messages"
echo "  - API: Retry attempts and circuit breaker logs"
