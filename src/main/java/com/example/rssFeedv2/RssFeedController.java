package com.example.rssFeedv2;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Set;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/rss")
public class RssFeedController {

    private final RedisTemplate<String, String> redisTemplate;

    public RssFeedController(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
/*

    @GetMapping("/api/redis/get/{key}")
    public String get(@PathVariable String key) {
        return redisTemplate.opsForValue().get(key);
    }
*/


    @GetMapping("/fetch-and-save-rss")
    public void fetchAndSaveRSS(@RequestParam String url) {
        try {
            SyndFeed feed = new SyndFeedInput().build(new XmlReader(new URL(url)));
            List<RSSItem> items = new ArrayList<>();

            for (SyndEntry entry : feed.getEntries()) {
                RSSItem item = new RSSItem();
                item.setId(UUID.randomUUID().toString());
                item.setTitle(entry.getTitle());
                item.setDescription(entry.getDescription() != null ? entry.getDescription().getValue() : "");
                item.setLink(entry.getLink());
                item.setPubDate(entry.getPublishedDate());
                
                // Extract tags from categories or generate from content
                List<String> tags = new ArrayList<>();
                if (entry.getCategories() != null && !entry.getCategories().isEmpty()) {
                    tags = entry.getCategories().stream()
                            .map(cat -> cat.getName())
                            .collect(Collectors.toList());
                } else {
                    // Generate tags from title and description
                    tags = extractTagsFromContent(item.getTitle(), item.getDescription());
                }
                item.setTags(tags);
                items.add(item);

                // Convert the RSSItem object to JSON
                String jsonItem = new ObjectMapper().writeValueAsString(item);

                redisTemplate.opsForValue().set(item.getId(), jsonItem);
                
                // Store item ID in a set for easy retrieval
                redisTemplate.opsForSet().add("rss:items", item.getId());
                
                // Store items by tags for quiz functionality
                for (String tag : tags) {
                    redisTemplate.opsForSet().add("rss:tag:" + tag.toLowerCase(), item.getId());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
/*
    @PostMapping("/api/redis/set/{key}")
    public void set(@PathVariable String key, @RequestBody String value) {
        redisTemplate.opsForValue().set(key, value);
    }

    @PutMapping("/api/redis/update/{key}")
    public void update(@PathVariable String key, @RequestBody String value) {
        redisTemplate.opsForValue().setIfPresent(key, value);
    }

    @DeleteMapping("/api/redis/delete/{key}")
    public void delete(@PathVariable String key) {
        redisTemplate.delete(key);
    }*/

    @GetMapping("/items")
    public ResponseEntity<List<RSSItem>> getAllItems() {
        try {
            Set<String> itemIds = redisTemplate.opsForSet().members("rss:items");
            List<RSSItem> items = new ArrayList<>();
            
            if (itemIds != null) {
                ObjectMapper mapper = new ObjectMapper();
                for (String itemId : itemIds) {
                    String jsonItem = redisTemplate.opsForValue().get(itemId);
                    if (jsonItem != null) {
                        RSSItem item = mapper.readValue(jsonItem, RSSItem.class);
                        items.add(item);
                    }
                }
            }
            
            return ResponseEntity.ok(items);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/items/by-tag")
    public ResponseEntity<List<RSSItem>> getItemsByTag(@RequestParam String tag) {
        try {
            Set<String> itemIds = redisTemplate.opsForSet().members("rss:tag:" + tag.toLowerCase());
            List<RSSItem> items = new ArrayList<>();
            
            if (itemIds != null) {
                ObjectMapper mapper = new ObjectMapper();
                for (String itemId : itemIds) {
                    String jsonItem = redisTemplate.opsForValue().get(itemId);
                    if (jsonItem != null) {
                        RSSItem item = mapper.readValue(jsonItem, RSSItem.class);
                        items.add(item);
                    }
                }
            }
            
            return ResponseEntity.ok(items);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/tags")
    public ResponseEntity<List<String>> getAllTags() {
        try {
            Set<String> keys = redisTemplate.keys("rss:tag:*");
            List<String> tags = new ArrayList<>();
            
            if (keys != null) {
                for (String key : keys) {
                    String tag = key.replace("rss:tag:", "");
                    tags.add(tag);
                }
            }
            
            return ResponseEntity.ok(tags);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/quiz")
    public ResponseEntity<RSSItem> getQuizItem(@RequestParam(required = false) String tag) {
        try {
            Set<String> itemIds;
            
            if (tag != null && !tag.isEmpty()) {
                itemIds = redisTemplate.opsForSet().members("rss:tag:" + tag.toLowerCase());
            } else {
                itemIds = redisTemplate.opsForSet().members("rss:items");
            }
            
            if (itemIds == null || itemIds.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            // Get random item
            String[] itemArray = itemIds.toArray(new String[0]);
            String randomItemId = itemArray[(int) (Math.random() * itemArray.length)];
            
            String jsonItem = redisTemplate.opsForValue().get(randomItemId);
            if (jsonItem != null) {
                ObjectMapper mapper = new ObjectMapper();
                RSSItem item = mapper.readValue(jsonItem, RSSItem.class);
                return ResponseEntity.ok(item);
            }
            
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    private List<String> extractTagsFromContent(String title, String description) {
        List<String> tags = new ArrayList<>();
        String content = (title + " " + description).toLowerCase();
        
        // Simple keyword extraction - you can enhance this with NLP libraries
        String[] keywords = {"technology", "ai", "machine learning", "software", "programming", 
                           "development", "javascript", "python", "java", "web", "mobile", 
                           "cloud", "security", "database", "api", "framework", "tutorial",
                           "news", "business", "science", "health", "education", "sports"};
        
        for (String keyword : keywords) {
            if (content.contains(keyword)) {
                tags.add(keyword);
            }
        }
        
        // If no tags found, add a general tag
        if (tags.isEmpty()) {
            tags.add("general");
        }
        
        return tags;
    }

}

