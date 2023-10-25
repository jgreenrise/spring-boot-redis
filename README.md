**README.md**

**Project Overview**

This project downloads the code from RSS feeds and saves it in Redis Cache using Spring Boot, Redis, and Gradle.

**External Tools**

* RedisLabs: https://app.redislabs.com
* Postman
* RedInsights

**Curl Command to Test**

**API: RSS FEED**
This will download the RSS feed from `https://example.com/rss.xml` and save the code in Redis Cache.

```
curl --location 'http://localhost:8080/api/rss/fetch-and-save-rss?url=https%3A%2F%2Fdeveloper.nvidia.com%2Fblog%2Ffeed%2F'
```

**API LeaderBoardSortedSet** 

***Add User to sortedSet***
```bash
curl --location --request POST 'http://localhost:8080/sortedset/add/mySortedSet?key=leaderboardOct2023&user=user1&score=1'
```

***Increment score***
```bash
curl --location --request PUT 'http://localhost:8080/sortedset/increment/mySortedSet?key=leaderboardOct2023&user=user1&score=1'
```

***Fetch Range of users by score***
```bash
curl --location --request GET 'http://localhost:8080/sortedset/range/mySortedSet?key=leaderboardOct2023&start=0&end=1000'
```

***Fetch range of users (Reverse)***
```bash
curl --location --request GET 'http://localhost:8080/sortedset/reverse-range/mySortedSet?key=leaderboardOct2023&start=0&end=1000'
```

***Fetch user rank***
```bash
curl --location --request GET 'http://localhost:8080/sortedset/rank/mySortedSet?key=leaderboardOct2023&user=user1'
```

**Getting Started**

To get started, you will need to install the following:

* Java 11 or higher
* Gradle 7 or higher
* Redis

Once you have installed the prerequisites, you can clone the project repository and build it using Gradle:

```
git clone https://github.com/BardAI/rss-feed-to-redis.git
cd rss-feed-to-redis
gradle build
```

Once the project is built, you can start the Spring Boot application using the following command:

```
java -jar build/libs/rss-feed-to-redis.jar
```

Once the application is running, you can use the curl command to test the functionality:

```
HTTP GET http://localhost:8080/api/rss/fetch-and-save-rss?url=<rss-feed-url>
```

You can then use RedInsights to view the saved code in Redis Cache.

**Usage**

To use the project, simply send a HTTP GET request to the `/api/rss/fetch-and-save-rss` endpoint with the `url`
parameter set to the URL of the RSS feed that you want to download.

For example, to download the RSS feed from `https://example.com/rss.xml` and save the code in Redis Cache, you would
send the following request:

```
HTTP GET http://localhost:8080/api/rss/fetch-and-save-rss?url=https://example.com/rss.xml
```

You can then use RedInsights to view the saved code in Redis Cache.

**Useful commands**

```
FLUSHDB // Delete all records from dB
SCAN 0 MATCH * // Get all records
```

