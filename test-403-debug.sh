#!/bin/bash

# 403 Error Debug Test Script
# This script tests POST /api/usuarios with different scenarios to isolate 403 issues

BASE_URL="http://localhost:8080"
ENDPOINT="/api/usuarios"
TEST_USER='{
  "nome": "Debug Test User",
  "email": "debug@test.com",
  "tipoUsuario": "ATLETA",
  "senha": "password123"
}'

echo "🔍 403 Error Debug Test Script"
echo "=============================="
echo ""

# Function to test endpoint
test_endpoint() {
    local description="$1"
    local extra_headers="$2"
    
    echo "🧪 Testing: $description"
    echo "----------------------------------------"
    
    echo "Request details:"
    echo "URL: $BASE_URL$ENDPOINT"
    echo "Method: POST"
    echo "Headers: Content-Type: application/json $extra_headers"
    echo "Body: $TEST_USER"
    echo ""
    
    response=$(curl -s -w "HTTPSTATUS:%{http_code}\nTIME:%{time_total}" \
        -X POST "$BASE_URL$ENDPOINT" \
        -H "Content-Type: application/json" \
        $extra_headers \
        -d "$TEST_USER")
    
    http_code=$(echo "$response" | tr -d '\n' | sed -e 's/.*HTTPSTATUS://' | sed -e 's/TIME:.*//')
    time_total=$(echo "$response" | tr -d '\n' | sed -e 's/.*TIME://')
    body=$(echo "$response" | sed -E 's/HTTPSTATUS:[0-9]{3}TIME:[0-9.]+//')
    
    echo "📊 Response:"
    echo "Status: $http_code"
    echo "Time: ${time_total}s"
    echo "Body: $body"
    echo ""
    
    case $http_code in
        201)
            echo "✅ SUCCESS: User created successfully"
            ;;
        403)
            echo "🚫 FORBIDDEN: This is the 403 error we're debugging"
            echo "💡 Possible causes:"
            echo "   - CORS preflight failure"
            echo "   - Security configuration blocking request"
            echo "   - Authentication/authorization issues"
            ;;
        400)
            echo "❌ BAD REQUEST: Check request format and validation"
            ;;
        404)
            echo "🔍 NOT FOUND: Check if endpoint exists and application is running"
            ;;
        500)
            echo "💥 INTERNAL ERROR: Check application logs"
            ;;
        *)
            echo "❓ UNEXPECTED STATUS: $http_code"
            ;;
    esac
    
    echo ""
    echo "========================================"
    echo ""
}

# Check if server is running
echo "🏁 Checking if application is running..."
health_check=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/actuator/health" 2>/dev/null)

if [ "$health_check" != "200" ]; then
    echo "❌ Application is not running at $BASE_URL"
    echo "   Please start the application first:"
    echo "   mvn spring-boot:run"
    echo ""
    exit 1
fi

echo "✅ Application is running"
echo ""

# Test 1: Basic POST request
test_endpoint "Basic POST request (no extra headers)"

# Test 2: With Origin header (simulates browser request)
test_endpoint "With Origin header (browser simulation)" "-H 'Origin: http://localhost:3000'"

# Test 3: With full CORS headers
test_endpoint "With full CORS headers" \
    "-H 'Origin: http://localhost:3000' -H 'Access-Control-Request-Method: POST' -H 'Access-Control-Request-Headers: Content-Type'"

# Test 4: OPTIONS preflight request
echo "🧪 Testing: OPTIONS preflight request"
echo "----------------------------------------"
echo "This tests if CORS preflight is working correctly"
echo ""

preflight_response=$(curl -s -w "HTTPSTATUS:%{http_code}" \
    -X OPTIONS "$BASE_URL$ENDPOINT" \
    -H "Origin: http://localhost:3000" \
    -H "Access-Control-Request-Method: POST" \
    -H "Access-Control-Request-Headers: Content-Type")

preflight_code=$(echo "$preflight_response" | tr -d '\n' | sed -e 's/.*HTTPSTATUS://')
preflight_body=$(echo "$preflight_response" | sed -E 's/HTTPSTATUS:[0-9]{3}//')

echo "📊 OPTIONS Response:"
echo "Status: $preflight_code"
echo "Body: $preflight_body"
echo ""

case $preflight_code in
    200)
        echo "✅ OPTIONS SUCCESS: CORS preflight is working"
        ;;
    403)
        echo "🚫 OPTIONS FORBIDDEN: CORS preflight is failing"
        echo "💡 This is likely the root cause of the POST 403 error"
        ;;
    404)
        echo "🔍 OPTIONS NOT FOUND: OPTIONS method may not be configured"
        ;;
    *)
        echo "❓ OPTIONS UNEXPECTED: $preflight_code"
        ;;
esac

echo ""
echo "========================================"
echo ""

# Summary and recommendations
echo "📋 DEBUGGING SUMMARY"
echo "===================="
echo ""
echo "If you're seeing 403 errors, try these debug profiles:"
echo ""
echo "1. Complete security bypass:"
echo "   mvn spring-boot:run -Dspring.profiles.active=debug"
echo ""
echo "2. CORS debugging:"
echo "   mvn spring-boot:run -Dspring.profiles.active=cors-debug"
echo ""
echo "3. Filter chain debugging:"
echo "   mvn spring-boot:run -Dspring.profiles.active=filter-debug"
echo ""
echo "4. Full debugging (combines all):"
echo "   mvn spring-boot:run -Dspring.profiles.active=dev,filter-debug,cors-debug"
echo ""
echo "Check the application logs for detailed debug information."
echo "See DEBUG_403_GUIDE.md for detailed analysis instructions."
echo ""
echo "🔧 To run this script: chmod +x test-403-debug.sh && ./test-403-debug.sh"