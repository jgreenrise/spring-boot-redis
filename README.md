**README.md**

**Project Overview**

This project downloads the code from RSS feeds and saves it in Redis Cache using Spring Boot, Redis, and Gradle.

**External Tools**

* RedisLabs: https://app.redislabs.com
* Postman
* RedInsights

**Curl Command to Test**

```
HTTP GET http://localhost:8080/api/rss/fetch-and-save-rss?url=<rss-feed-url>
```

**Example**

```
HTTP GET http://localhost:8080/api/rss/fetch-and-save-rss?url=https://example.com/rss.xml
```

This will download the RSS feed from `https://example.com/rss.xml` and save the code in Redis Cache.

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