**README.md**

**Project Overview**

This is a Flash Card API application built with Spring Boot, Redis, and Gradle. The application provides RESTful APIs to create, read, update, and delete flash cards. Each flash card contains a question, answer, category, and difficulty level, making it perfect for learning and study applications.

**External Tools**

* RedisLabs: https://app.redislabs.com
* Postman
* RedInsights

**Flash Card API Documentation**

## API Endpoints

### **Create Flash Card**
Create a new flash card with question, answer, category, and difficulty.

```bash
curl --location --request POST 'http://localhost:8080/api/flashcards' \
--header 'Content-Type: application/json' \
--data-raw '{
    "question": "What is the capital of France?",
    "answer": "Paris",
    "category": "Geography",
    "difficulty": "EASY"
}'
```

### **Get Flash Card by ID**
Retrieve a specific flash card by its ID.

```bash
curl --location --request GET 'http://localhost:8080/api/flashcards/{flashcard-id}'
```

### **Get All Flash Cards**
Retrieve all flash cards in the system.

```bash
curl --location --request GET 'http://localhost:8080/api/flashcards'
```

### **Get Flash Cards by Category**
Retrieve all flash cards in a specific category.

```bash
curl --location --request GET 'http://localhost:8080/api/flashcards/category/Geography'
```

### **Get Flash Cards by Difficulty**
Retrieve all flash cards with a specific difficulty level.

```bash
curl --location --request GET 'http://localhost:8080/api/flashcards/difficulty/EASY'
```

### **Update Flash Card**
Update an existing flash card.

```bash
curl --location --request PUT 'http://localhost:8080/api/flashcards/{flashcard-id}' \
--header 'Content-Type: application/json' \
--data-raw '{
    "question": "What is the capital of France?",
    "answer": "Paris is the capital and most populous city of France",
    "category": "Geography",
    "difficulty": "MEDIUM"
}'
```

### **Delete Flash Card**
Delete a specific flash card by its ID.

```bash
curl --location --request DELETE 'http://localhost:8080/api/flashcards/{flashcard-id}'
```

### **Get Flash Card Count**
Get the total number of flash cards in the system.

```bash
curl --location --request GET 'http://localhost:8080/api/flashcards/count'
```

### **Get Random Flash Card**
Get a random flash card for studying.

```bash
curl --location --request GET 'http://localhost:8080/api/flashcards/random'
```

### **Delete All Flash Cards**
Delete all flash cards from the system (use with caution).

```bash
curl --location --request DELETE 'http://localhost:8080/api/flashcards'
```

## Flash Card Model

```json
{
    "id": "uuid-string",
    "question": "What is the capital of France?",
    "answer": "Paris",
    "category": "Geography",
    "difficulty": "EASY",
    "createdAt": "2023-12-01 10:30:00",
    "updatedAt": "2023-12-01 10:30:00"
}
```

### Difficulty Levels
- **EASY** - Basic level questions
- **MEDIUM** - Intermediate level questions  
- **HARD** - Advanced level questions

**Getting Started**

To get started, you will need to install the following:

* Java 11 or higher
* Gradle 7 or higher
* Redis

Once you have installed the prerequisites, you can clone the project repository and build it using Gradle:

```bash
git clone <your-repository-url>
cd flashcard-api
gradle build
```

Once the project is built, you can start the Spring Boot application using the following command:

```bash
java -jar build/libs/rssFeedv2-0.0.1-SNAPSHOT.jar
```

Or you can run it directly with Gradle:

```bash
./gradlew bootRun
```

Once the application is running, you can test the flash card APIs using the curl commands provided above.

**Usage**

To use the Flash Card API, you can interact with the following endpoints:

1. **Create flash cards** by sending POST requests to `/api/flashcards`
2. **Retrieve flash cards** by sending GET requests to various endpoints
3. **Update flash cards** by sending PUT requests to `/api/flashcards/{id}`
4. **Delete flash cards** by sending DELETE requests to `/api/flashcards/{id}`

### Example Usage Flow:

1. Create a new flash card:
```bash
curl -X POST http://localhost:8080/api/flashcards \
-H "Content-Type: application/json" \
-d '{"question":"What is 2+2?","answer":"4","category":"Math","difficulty":"EASY"}'
```

2. Get all flash cards:
```bash
curl http://localhost:8080/api/flashcards
```

3. Get a random flash card for studying:
```bash
curl http://localhost:8080/api/flashcards/random
```

You can use RedInsights or any Redis CLI tool to view the stored data in Redis Cache.

**Useful commands**

```
FLUSHDB // Delete all records from dB
SCAN 0 MATCH * // Get all records
```

