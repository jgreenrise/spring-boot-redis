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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
                item.setDescription(entry.getDescription().getValue());
                item.setLink(entry.getLink());
                item.setPubDate(entry.getPublishedDate());
                items.add(item);

                // Convert the RSSItem object to JSON
                String jsonItem = new ObjectMapper().writeValueAsString(item);

                redisTemplate.opsForValue().set(item.getId(), jsonItem);
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

}

