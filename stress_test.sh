#!/bin/bash

echo "🚀 Starting Stress Test..."

# Test 1: Concurrent reads (should use cache after first request)
echo "Test 1: 50 concurrent GET requests"
for i in {1..50}; do
    curl -s "http://localhost:8111/api/v1/employee" > /dev/null &
done
wait
echo "✅ Concurrent reads completed"

# Test 2: Search validation stress
echo "Test 2: Invalid input validation (should all return 400)"
for i in {1..10}; do
    curl -s -w "%{http_code} " "http://localhost:8111/api/v1/employee/search/test$i" > /dev/null &
done
wait
echo ""
echo "✅ Validation stress completed"

# Test 3: Cache hit test
echo "Test 3: Cache effectiveness test"
time curl -s "http://localhost:8111/api/v1/employee/search/Idella" > /dev/null
time curl -s "http://localhost:8111/api/v1/employee/search/Idella" > /dev/null
echo "✅ Cache test completed (second should be faster)"

echo "🎯 Stress test completed!"
