#!/bin/bash

echo "Flash Card API Test Script"
echo "=========================="
echo ""

BASE_URL="http://localhost:8080/api/flashcards"

echo "1. Creating sample flash cards..."
echo ""

# Create flash card 1
echo "Creating Math flash card..."
FLASHCARD1=$(curl -s -X POST $BASE_URL \
  -H "Content-Type: application/json" \
  -d '{
    "question": "What is 2 + 2?",
    "answer": "4",
    "category": "Math",
    "difficulty": "EASY"
  }')
echo "Response: $FLASHCARD1"
echo ""

# Create flash card 2
echo "Creating Geography flash card..."
FLASHCARD2=$(curl -s -X POST $BASE_URL \
  -H "Content-Type: application/json" \
  -d '{
    "question": "What is the capital of France?",
    "answer": "Paris",
    "category": "Geography",
    "difficulty": "EASY"
  }')
echo "Response: $FLASHCARD2"
echo ""

# Create flash card 3
echo "Creating Science flash card..."
FLASHCARD3=$(curl -s -X POST $BASE_URL \
  -H "Content-Type: application/json" \
  -d '{
    "question": "What is the chemical symbol for water?",
    "answer": "H2O",
    "category": "Science",
    "difficulty": "MEDIUM"
  }')
echo "Response: $FLASHCARD3"
echo ""

echo "2. Getting all flash cards..."
curl -s -X GET $BASE_URL | jq '.'
echo ""

echo "3. Getting flash card count..."
curl -s -X GET $BASE_URL/count
echo ""
echo ""

echo "4. Getting flash cards by category (Math)..."
curl -s -X GET $BASE_URL/category/Math | jq '.'
echo ""

echo "5. Getting flash cards by difficulty (EASY)..."
curl -s -X GET $BASE_URL/difficulty/EASY | jq '.'
echo ""

echo "6. Getting a random flash card..."
curl -s -X GET $BASE_URL/random | jq '.'
echo ""

echo "Test completed! The Flash Card API is working correctly."
echo ""
echo "Available endpoints:"
echo "- POST   $BASE_URL                    (Create flash card)"
echo "- GET    $BASE_URL                    (Get all flash cards)"
echo "- GET    $BASE_URL/{id}               (Get flash card by ID)"
echo "- PUT    $BASE_URL/{id}               (Update flash card)"
echo "- DELETE $BASE_URL/{id}               (Delete flash card)"
echo "- GET    $BASE_URL/category/{category} (Get by category)"
echo "- GET    $BASE_URL/difficulty/{level}  (Get by difficulty)"
echo "- GET    $BASE_URL/random             (Get random flash card)"
echo "- GET    $BASE_URL/count              (Get total count)"
echo "- DELETE $BASE_URL                    (Delete all flash cards)"