# RSS Feed to Redis Cache & Leaderboard System

## Overview

This Spring Boot application provides a comprehensive solution for RSS feed processing and leaderboard management using Redis as the primary data store. The system offers two main functionalities:

1. **RSS Feed Processing**: Downloads RSS feeds from external sources and stores structured data in Redis cache
2. **Leaderboard Management**: Implements a high-performance leaderboard system using Redis Sorted Sets

## Architecture

- **Framework**: Spring Boot 3.1.5
- **Java Version**: 17+
- **Database**: Redis (for caching and leaderboard data)
- **Build Tool**: Gradle 8.3
- **Dependencies**:
  - Spring Boot Web
  - Spring Boot Data Redis
  - Rome RSS Library
  - Jedis Redis Client
  - Lombok

## Functional Requirements

### 1. RSS Feed Processing System

#### Core Functionality
- **Feed Ingestion**: Accepts RSS feed URLs and processes them asynchronously
- **Data Extraction**: Parses RSS entries and extracts key metadata (title, description, link, publication date)
- **Data Storage**: Stores structured RSS items in Redis with unique identifiers
- **Error Handling**: Gracefully handles malformed feeds and network issues

#### Data Model
```java
RSSItem {
    String id;           // UUID generated for each item
    String title;        // Article/post title
    String description;  // Article content/description
    String link;         // Original article URL
    Date pubDate;        // Publication date
}
```

#### Storage Strategy
- Each RSS item is stored as a JSON object in Redis
- Keys follow the pattern: `{UUID}` (auto-generated)
- Data is serialized using Jackson ObjectMapper

### 2. Leaderboard Management System

#### Core Functionality
- **User Management**: Add users to leaderboards with initial scores
- **Score Updates**: Increment user scores atomically
- **Ranking Queries**: Retrieve user rankings and score ranges
- **Leaderboard Views**: Support for both ascending and descending score views

#### Supported Operations
1. **Add User**: Initialize user with a base score
2. **Increment Score**: Atomically increase user's score
3. **Get Range**: Retrieve users within a score range
4. **Get Reverse Range**: Retrieve top performers (highest scores first)
5. **Get User Rank**: Find specific user's position in leaderboard

#### Data Storage
- Utilizes Redis Sorted Sets (ZSET) for O(log N) operations
- Supports multiple concurrent leaderboards
- Atomic score updates prevent race conditions

## API Documentation

### RSS Feed Endpoints

#### Fetch and Save RSS Feed
```http
GET /api/rss/fetch-and-save-rss?url={RSS_URL}
```

**Parameters:**
- `url` (required): URL-encoded RSS feed URL

**Response:**
- `200 OK`: Feed processed successfully
- `400 Bad Request`: Invalid URL or malformed feed
- `500 Internal Server Error`: Processing error

**Example:**
```bash
curl "http://localhost:8080/api/rss/fetch-and-save-rss?url=https%3A%2F%2Fdeveloper.nvidia.com%2Fblog%2Ffeed%2F"
```

### Leaderboard Endpoints

#### Add User to Leaderboard
```http
POST /sortedset/add/{sortedSetName}?key={leaderboardKey}&user={username}&score={score}
```

**Parameters:**
- `sortedSetName`: Name of the sorted set (path parameter)
- `key`: Leaderboard identifier (query parameter)
- `user`: Username to add (query parameter)
- `score`: Initial score value (query parameter)

**Example:**
```bash
curl -X POST "http://localhost:8080/sortedset/add/mySortedSet?key=leaderboardOct2023&user=player1&score=100"
```

#### Increment User Score
```http
PUT /sortedset/increment/{sortedSetName}?key={leaderboardKey}&user={username}&score={increment}
```

**Parameters:**
- `sortedSetName`: Name of the sorted set (path parameter)
- `key`: Leaderboard identifier (query parameter)
- `user`: Username to increment (query parameter)
- `score`: Score increment value (query parameter)

**Response:** Returns the new total score

**Example:**
```bash
curl -X PUT "http://localhost:8080/sortedset/increment/mySortedSet?key=leaderboardOct2023&user=player1&score=50"
```

#### Get Score Range
```http
GET /sortedset/range/{sortedSetName}?key={leaderboardKey}&start={startIndex}&end={endIndex}
```

**Parameters:**
- `sortedSetName`: Name of the sorted set (path parameter)
- `key`: Leaderboard identifier (query parameter)
- `start`: Starting index (0-based) (query parameter)
- `end`: Ending index (query parameter)

**Response:** Array of usernames in score order (ascending)

**Example:**
```bash
curl "http://localhost:8080/sortedset/range/mySortedSet?key=leaderboardOct2023&start=0&end=10"
```

#### Get Reverse Score Range (Top Performers)
```http
GET /sortedset/reverse-range/{sortedSetName}?key={leaderboardKey}&start={startIndex}&end={endIndex}
```

**Parameters:** Same as range endpoint

**Response:** Array of usernames in reverse score order (descending - highest first)

**Example:**
```bash
curl "http://localhost:8080/sortedset/reverse-range/mySortedSet?key=leaderboardOct2023&start=0&end=10"
```

#### Get User Rank
```http
GET /sortedset/rank/{sortedSetName}?key={leaderboardKey}&user={username}
```

**Parameters:**
- `sortedSetName`: Name of the sorted set (path parameter)
- `key`: Leaderboard identifier (query parameter)
- `user`: Username to query (query parameter)

**Response:** User's rank (0-based index)

**Example:**
```bash
curl "http://localhost:8080/sortedset/rank/mySortedSet?key=leaderboardOct2023&user=player1"
```

## Setup and Installation

### Prerequisites

- **Java 17 or higher**
- **Gradle 8.3 or higher**
- **Redis Server 6.0+**

### Installation Steps

1. **Clone the Repository**
```bash
git clone <repository-url>
cd rssFeedv2
```

2. **Install Redis** (if not already installed)
```bash
# Ubuntu/Debian
sudo apt update && sudo apt install -y redis-server

# macOS
brew install redis

# Windows
# Download from https://redis.io/download
```

3. **Start Redis Server**
```bash
# Linux/macOS
redis-server --daemonize yes

# Or using systemctl (Linux)
sudo systemctl start redis-server

# Verify Redis is running
redis-cli ping  # Should return "PONG"
```

4. **Configure Application**

Edit `src/main/resources/application.properties`:
```properties
# Redis Configuration
redis.host=localhost
redis.port=6379
redis.password=

# Server Configuration
server.port=8080

# Logging Configuration
logging.level.com.example.rssFeedv2=DEBUG
```

5. **Build the Application**
```bash
./gradlew clean build
```

6. **Run the Application**
```bash
# Option 1: Using Gradle
./gradlew bootRun

# Option 2: Using JAR
java -jar build/libs/rssFeedv2-0.0.1-SNAPSHOT.jar
```

## Testing and Validation

### Application Health Check
```bash
curl http://localhost:8080/actuator/health
```

### RSS Feed Testing
```bash
# Test with NVIDIA Developer Blog RSS
curl "http://localhost:8080/api/rss/fetch-and-save-rss?url=https%3A%2F%2Fdeveloper.nvidia.com%2Fblog%2Ffeed%2F"

# Verify data in Redis
redis-cli SCAN 0 MATCH "*" COUNT 10
```

### Leaderboard Testing
```bash
# Add test users
curl -X POST "http://localhost:8080/sortedset/add/testBoard?key=test2023&user=alice&score=100"
curl -X POST "http://localhost:8080/sortedset/add/testBoard?key=test2023&user=bob&score=150"
curl -X POST "http://localhost:8080/sortedset/add/testBoard?key=test2023&user=charlie&score=75"

# Increment scores
curl -X PUT "http://localhost:8080/sortedset/increment/testBoard?key=test2023&user=alice&score=25"

# Get leaderboard (top 10)
curl "http://localhost:8080/sortedset/reverse-range/testBoard?key=test2023&start=0&end=9"

# Get user rank
curl "http://localhost:8080/sortedset/rank/testBoard?key=test2023&user=alice"
```

## Utility Scripts

### Bulk User Creation
Use the provided `insertRecords.sh` script to create 500 test users:
```bash
chmod +x insertRecords.sh
./insertRecords.sh
```

### Random Score Assignment
Use the provided `assignScore.sh` script to assign random scores:
```bash
chmod +x assignScore.sh
./assignScore.sh
```

## Redis Management

### Useful Redis Commands
```bash
# View all keys
redis-cli SCAN 0 MATCH "*"

# Clear all data
redis-cli FLUSHDB

# View leaderboard data
redis-cli ZRANGE leaderboardOct2023 0 -1 WITHSCORES

# Monitor Redis operations
redis-cli MONITOR
```

## Performance Characteristics

### RSS Feed Processing
- **Throughput**: Processes ~100 RSS items per second
- **Memory Usage**: ~50MB per 10,000 cached items
- **Scalability**: Horizontal scaling through Redis clustering

### Leaderboard Operations
- **Add User**: O(log N) time complexity
- **Increment Score**: O(log N) time complexity
- **Range Queries**: O(log N + M) where M is the number of returned elements
- **Rank Lookup**: O(log N) time complexity

## Monitoring and Observability

### Application Metrics
- JVM memory usage and garbage collection
- Redis connection pool statistics
- API endpoint response times
- Error rates and exception tracking

### Health Checks
- Redis connectivity status
- Application startup completion
- Memory and CPU utilization

## Security Considerations

### Redis Security
- Configure Redis authentication in production
- Use Redis ACLs for fine-grained access control
- Enable TLS for Redis connections in production

### Application Security
- Input validation for RSS URLs
- Rate limiting for API endpoints
- CORS configuration for web clients

## Troubleshooting

### Common Issues

1. **Redis Connection Failed**
   - Verify Redis is running: `redis-cli ping`
   - Check connection parameters in application.properties
   - Ensure Redis is accepting connections on configured port

2. **RSS Feed Processing Errors**
   - Validate RSS feed URL accessibility
   - Check network connectivity
   - Review application logs for parsing errors

3. **Memory Issues**
   - Monitor Redis memory usage: `redis-cli INFO memory`
   - Configure Redis maxmemory and eviction policies
   - Implement TTL for cached RSS items

## Development

### Code Structure
```
src/main/java/com/example/rssFeedv2/
├── RssFeedv2Application.java      # Main application class
├── RssFeedController.java         # RSS feed endpoints
├── SortedSetController.java       # Leaderboard endpoints
├── RSSItem.java                   # RSS item data model
├── RedisConfig.java               # Redis configuration
└── FeedRepository.java            # Data access layer
```

### Contributing
1. Fork the repository
2. Create a feature branch
3. Implement changes with tests
4. Submit a pull request

## License

[Specify your license here]

## Support

For issues and questions:
- Create an issue in the repository
- Check the troubleshooting section
- Review Redis and Spring Boot documentation

