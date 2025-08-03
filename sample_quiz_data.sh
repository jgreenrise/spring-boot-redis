#!/bin/bash

# Sample Quiz Data and Testing Script for Anki-style Quiz Mode
# Make sure your Spring Boot application is running on localhost:8080

echo "🃏 Anki-Style Quiz Mode Demo"
echo "=================================="

BASE_URL="http://localhost:8080/api/quiz"

echo ""
echo "📝 Creating Sample Quiz Cards..."

# Create Math Cards
echo "Creating Math cards..."
curl -X POST "$BASE_URL/cards" \
  -H "Content-Type: application/json" \
  -d '{
    "question": "What is 7 × 8?",
    "answer": "56",
    "category": "math",
    "difficulty": 2
  }' | jq '.'

curl -X POST "$BASE_URL/cards" \
  -H "Content-Type: application/json" \
  -d '{
    "question": "What is the derivative of x²?",
    "answer": "2x",
    "category": "math",
    "difficulty": 4
  }' | jq '.'

curl -X POST "$BASE_URL/cards" \
  -H "Content-Type: application/json" \
  -d '{
    "question": "What is the square root of 144?",
    "answer": "12",
    "category": "math",
    "difficulty": 1
  }' | jq '.'

# Create Science Cards
echo "Creating Science cards..."
curl -X POST "$BASE_URL/cards" \
  -H "Content-Type: application/json" \
  -d '{
    "question": "What is the chemical symbol for gold?",
    "answer": "Au",
    "category": "science",
    "difficulty": 2
  }' | jq '.'

curl -X POST "$BASE_URL/cards" \
  -H "Content-Type: application/json" \
  -d '{
    "question": "What is the speed of light in vacuum?",
    "answer": "299,792,458 m/s",
    "category": "science",
    "difficulty": 3
  }' | jq '.'

# Create Programming Cards
echo "Creating Programming cards..."
curl -X POST "$BASE_URL/cards" \
  -H "Content-Type: application/json" \
  -d '{
    "question": "What does REST stand for?",
    "answer": "Representational State Transfer",
    "category": "programming",
    "difficulty": 2
  }' | jq '.'

curl -X POST "$BASE_URL/cards" \
  -H "Content-Type: application/json" \
  -d '{
    "question": "What is the time complexity of binary search?",
    "answer": "O(log n)",
    "category": "programming",
    "difficulty": 3
  }' | jq '.'

echo ""
echo "📊 Getting Available Categories..."
curl -X GET "$BASE_URL/categories" | jq '.'

echo ""
echo "🆕 Getting New Cards (never studied)..."
curl -X GET "$BASE_URL/cards/new?limit=5" | jq '.'

echo ""
echo "🎯 Starting a Mixed Quiz Session..."
SESSION_RESPONSE=$(curl -s -X POST "$BASE_URL/sessions/start" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "testuser1",
    "category": "",
    "sessionType": "mixed",
    "maxCards": 5
  }')

echo "$SESSION_RESPONSE" | jq '.'

# Extract session ID for further testing
SESSION_ID=$(echo "$SESSION_RESPONSE" | jq -r '.sessionId')

echo ""
echo "📋 Session ID: $SESSION_ID"

echo ""
echo "🎴 Getting Current Card in Session..."
curl -X GET "$BASE_URL/sessions/$SESSION_ID/current" | jq '.'

echo ""
echo "✅ Submitting Answer (Quality: 4 - Correct)..."
curl -X POST "$BASE_URL/sessions/$SESSION_ID/answer" \
  -H "Content-Type: application/json" \
  -d '{
    "quality": 4
  }' | jq '.'

echo ""
echo "📈 Getting Session Status..."
curl -X GET "$BASE_URL/sessions/$SESSION_ID/status" | jq '.'

echo ""
echo "📊 Getting User Statistics..."
curl -X GET "$BASE_URL/users/testuser1/stats" | jq '.'

echo ""
echo "🏆 Quiz Commands Reference:"
echo "=================================="
echo ""
echo "🔹 Card Management:"
echo "  Create Card: POST $BASE_URL/cards"
echo "  Get Card: GET $BASE_URL/cards/{cardId}"
echo "  Update Card: PUT $BASE_URL/cards/{cardId}"
echo "  Delete Card: DELETE $BASE_URL/cards/{cardId}"
echo ""
echo "🔹 Quiz Sessions (Anki-style):"
echo "  Start Session: POST $BASE_URL/sessions/start"
echo "  Get Current Card: GET $BASE_URL/sessions/{sessionId}/current"
echo "  Submit Answer: POST $BASE_URL/sessions/{sessionId}/answer"
echo "  Session Status: GET $BASE_URL/sessions/{sessionId}/status"
echo ""
echo "🔹 Analytics:"
echo "  User Stats: GET $BASE_URL/users/{userId}/stats"
echo "  Categories: GET $BASE_URL/categories"
echo "  Due Cards: GET $BASE_URL/cards/due"
echo "  New Cards: GET $BASE_URL/cards/new"
echo ""
echo "🔹 Anki Quality Scale (0-5):"
echo "  0 = Complete blackout"
echo "  1 = Incorrect, no recognition"
echo "  2 = Incorrect, but familiar"
echo "  3 = Correct with difficulty"
echo "  4 = Correct with hesitation"
echo "  5 = Perfect recall"
echo ""
echo "✨ The system uses Anki's spaced repetition algorithm:"
echo "   - Cards you get wrong appear more frequently"
echo "   - Cards you get right have increasing intervals"
echo "   - Easiness factor adjusts based on your performance"
echo "   - New cards are mixed with review cards"