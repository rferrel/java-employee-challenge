#!/bin/bash

echo "🔥 AGGRESSIVE Stress Test - Triggering Rate Limits..."

# Clear cache first by creating an employee (invalidates cache)
echo "Clearing cache..."
curl -s -X POST "http://localhost:8111/api/v1/employee" \
  -H "Content-Type: application/json" \
  -d '{"name":"StressTest","salary":50000,"age":25,"title":"Tester"}' > /dev/null

# Test 1: Massive concurrent load to trigger rate limiting
echo "Test 1: 200 concurrent requests (should trigger rate limiting)"
for i in {1..200}; do
    curl -s "http://localhost:8111/api/v1/employee" > /dev/null &
    if [ $((i % 20)) -eq 0 ]; then
        echo "Launched $i requests..."
    fi
done
wait
echo "✅ 200 concurrent requests completed"

# Test 2: Rapid sequential requests
echo "Test 2: 100 rapid sequential requests"
for i in {1..100}; do
    curl -s "http://localhost:8111/api/v1/employee/highestSalary" > /dev/null
    if [ $((i % 10)) -eq 0 ]; then
        echo "Completed $i sequential requests..."
    fi
done
echo "✅ Sequential requests completed"

# Test 3: Mixed load with cache invalidation
echo "Test 3: Mixed load with cache invalidation"
for i in {1..50}; do
    # Mix of reads and writes to trigger cache invalidation
    curl -s "http://localhost:8111/api/v1/employee" > /dev/null &
    curl -s "http://localhost:8111/api/v1/employee/search/Test" > /dev/null &
    
    # Every 10th request, try to create (will invalidate cache)
    if [ $((i % 10)) -eq 0 ]; then
        curl -s -X POST "http://localhost:8111/api/v1/employee" \
          -H "Content-Type: application/json" \
          -d "{\"name\":\"Load$i\",\"salary\":$((50000+i)),\"age\":25,\"title\":\"LoadTester\"}" > /dev/null &
        echo "Cache invalidation $i triggered..."
    fi
done
wait
echo "✅ Mixed load completed"

echo "🎯 Check your API terminal for Resilience4j retry logs!"
