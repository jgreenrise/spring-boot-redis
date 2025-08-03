# Flash Card Feed API Documentation

This document describes the flash card functionality that has been integrated into the RSS Feed application.

## Overview

The application now supports flash cards that can be managed through REST APIs and included in feed responses. Flash cards contain questions, answers, categories, difficulty levels, and tracking information.

## Flash Card Entity

A flash card contains the following fields:
- `id`: Unique identifier
- `question`: The question text
- `answer`: The answer text
- `category`: Category (e.g., "Mathematics", "Geography", "Programming")
- `difficulty`: Difficulty level ("EASY", "MEDIUM", "HARD")
- `createdDate`: When the card was created
- `lastReviewed`: When the card was last reviewed
- `reviewCount`: Number of times the card has been reviewed
- `isActive`: Whether the card is active (visible in feeds)

## API Endpoints

### Flash Card Management

#### Create Flash Card
```
POST /api/flashcards
Content-Type: application/json

{
  "question": "What is the capital of France?",
  "answer": "Paris",
  "category": "Geography",
  "difficulty": "EASY"
}
```

#### Get All Flash Cards
```
GET /api/flashcards
```

#### Get Flash Card by ID
```
GET /api/flashcards/{id}
```

#### Update Flash Card
```
PUT /api/flashcards/{id}
Content-Type: application/json

{
  "question": "Updated question",
  "answer": "Updated answer",
  "category": "Updated category",
  "difficulty": "MEDIUM",
  "isActive": true
}
```

#### Delete Flash Card
```
DELETE /api/flashcards/{id}
```

#### Mark Flash Card as Reviewed
```
POST /api/flashcards/{id}/review
```

#### Get Flash Cards by Category
```
GET /api/flashcards/category/{category}
```

#### Get Flash Cards by Difficulty
```
GET /api/flashcards/difficulty/{difficulty}
```

#### Get Random Flash Cards
```
GET /api/flashcards/random?count=10
```

#### Bulk Create Flash Cards
```
POST /api/flashcards/bulk
Content-Type: application/json

[
  {
    "question": "Question 1",
    "answer": "Answer 1",
    "category": "Category 1",
    "difficulty": "EASY"
  },
  {
    "question": "Question 2",
    "answer": "Answer 2",
    "category": "Category 2",
    "difficulty": "MEDIUM"
  }
]
```

### Feed Endpoints

#### Combined Feed (RSS Items + Flash Cards)
```
GET /api/rss/feed?limit=20
```
Returns a combined feed containing both RSS items and flash cards, sorted by publication/creation date.

#### Flash Cards Only Feed
```
GET /api/rss/flashcards-feed?limit=10&category=Mathematics&difficulty=EASY
```
Returns only flash cards, with optional filtering by category and difficulty.

## Feed Item Structure

The unified feed returns `FeedItem` objects with the following structure:

```json
{
  "id": "unique-id",
  "title": "Flash Card: Geography",
  "description": "Q: What is the capital of France? | A: Paris",
  "link": "/api/flashcards/unique-id",
  "pubDate": "2024-01-15T10:30:00Z",
  "type": "FLASHCARD",
  "category": "Geography",
  "difficulty": "EASY"
}
```

- `type`: Either "RSS" or "FLASHCARD"
- `category` and `difficulty`: Only present for flash card items

## Sample Data

The application automatically creates sample flash cards on startup if none exist, covering various categories:
- Geography
- Mathematics
- Literature
- Chemistry
- History
- Physics
- Astronomy
- Programming

## Usage Examples

1. **Get all flash cards**: `GET /api/flashcards`
2. **Get geography flash cards**: `GET /api/flashcards/category/Geography`
3. **Get easy difficulty cards**: `GET /api/flashcards/difficulty/EASY`
4. **Get combined feed**: `GET /api/rss/feed?limit=15`
5. **Get flash cards feed only**: `GET /api/rss/flashcards-feed?limit=5&category=Mathematics`

## Storage

Flash cards are stored in Redis using Spring Data Redis, providing fast access and persistence. Each flash card is also cached in Redis with the key pattern `flashcard:{id}` for quick retrieval.