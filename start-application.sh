#!/bin/bash

echo "🚀 Starting RSS Feed Hub Application"
echo "======================================"

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "❌ Java is not installed. Please install Java 11 or higher."
    exit 1
fi

# Check if Node.js is installed
if ! command -v node &> /dev/null; then
    echo "❌ Node.js is not installed. Please install Node.js 16 or higher."
    exit 1
fi

# Check if Redis is running (optional check)
if ! redis-cli ping &> /dev/null; then
    echo "⚠️  Redis is not running. Please start Redis server for full functionality."
    echo "   You can start Redis with: redis-server"
    echo ""
fi

echo "📦 Building Spring Boot backend..."
./gradlew build

if [ $? -ne 0 ]; then
    echo "❌ Failed to build Spring Boot application"
    exit 1
fi

echo "🔧 Installing frontend dependencies..."
cd twitter-clone
npm install

if [ $? -ne 0 ]; then
    echo "❌ Failed to install frontend dependencies"
    exit 1
fi

cd ..

echo "🎯 Starting applications..."
echo ""
echo "Backend will run on: http://localhost:8080"
echo "Frontend will run on: http://localhost:3000"
echo ""
echo "Press Ctrl+C to stop both applications"
echo ""

# Start backend in background
echo "Starting Spring Boot backend..."
java -jar build/libs/*.jar &
BACKEND_PID=$!

# Wait a moment for backend to start
sleep 5

# Start frontend
echo "Starting React frontend..."
cd twitter-clone
npm start &
FRONTEND_PID=$!

# Function to cleanup on exit
cleanup() {
    echo ""
    echo "🛑 Stopping applications..."
    kill $BACKEND_PID 2>/dev/null
    kill $FRONTEND_PID 2>/dev/null
    echo "✅ Applications stopped"
    exit 0
}

# Set trap to cleanup on Ctrl+C
trap cleanup SIGINT

# Wait for both processes
wait