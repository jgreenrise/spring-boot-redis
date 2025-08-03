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
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rss")
public class RssFeedController {

    private final RedisTemplate<String, String> redisTemplate;
    
    @Autowired
    private FlashCardRepository flashCardRepository;

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

    // Get combined feed with RSS items and flash cards
    @GetMapping("/feed")
    public List<FeedItem> getCombinedFeed(@RequestParam(defaultValue = "20") int limit) {
        List<FeedItem> feedItems = new ArrayList<>();
        
        try {
            // Get RSS items from Redis
            List<RSSItem> rssItems = getRSSItemsFromRedis();
            for (RSSItem rssItem : rssItems) {
                FeedItem feedItem = new FeedItem();
                feedItem.setId(rssItem.getId());
                feedItem.setTitle(rssItem.getTitle());
                feedItem.setDescription(rssItem.getDescription());
                feedItem.setLink(rssItem.getLink());
                feedItem.setPubDate(rssItem.getPubDate());
                feedItem.setType("RSS");
                feedItems.add(feedItem);
            }
            
            // Get flash cards
            List<FlashCard> flashCards = new ArrayList<>();
            flashCardRepository.findAll().forEach(flashCards::add);
            
            List<FlashCard> activeFlashCards = flashCards.stream()
                    .filter(FlashCard::isActive)
                    .collect(Collectors.toList());
            
            for (FlashCard flashCard : activeFlashCards) {
                FeedItem feedItem = new FeedItem();
                feedItem.setId(flashCard.getId());
                feedItem.setTitle("Flash Card: " + flashCard.getCategory());
                feedItem.setDescription("Q: " + flashCard.getQuestion() + " | A: " + flashCard.getAnswer());
                feedItem.setLink("/api/flashcards/" + flashCard.getId());
                feedItem.setPubDate(flashCard.getCreatedDate());
                feedItem.setType("FLASHCARD");
                feedItem.setCategory(flashCard.getCategory());
                feedItem.setDifficulty(flashCard.getDifficulty());
                feedItems.add(feedItem);
            }
            
            // Sort by publication date (newest first) and limit results
            feedItems.sort((a, b) -> {
                if (a.getPubDate() == null && b.getPubDate() == null) return 0;
                if (a.getPubDate() == null) return 1;
                if (b.getPubDate() == null) return -1;
                return b.getPubDate().compareTo(a.getPubDate());
            });
            
            return feedItems.stream().limit(limit).collect(Collectors.toList());
            
        } catch (Exception e) {
            e.printStackTrace();
            return feedItems;
        }
    }
    
    // Get only flash cards feed
    @GetMapping("/flashcards-feed")
    public List<FeedItem> getFlashCardsFeed(@RequestParam(defaultValue = "10") int limit,
                                           @RequestParam(required = false) String category,
                                           @RequestParam(required = false) String difficulty) {
        List<FeedItem> feedItems = new ArrayList<>();
        
        try {
            List<FlashCard> flashCards = new ArrayList<>();
            flashCardRepository.findAll().forEach(flashCards::add);
            
            List<FlashCard> filteredCards = flashCards.stream()
                    .filter(FlashCard::isActive)
                    .filter(card -> category == null || category.equalsIgnoreCase(card.getCategory()))
                    .filter(card -> difficulty == null || difficulty.equalsIgnoreCase(card.getDifficulty()))
                    .collect(Collectors.toList());
            
            for (FlashCard flashCard : filteredCards) {
                FeedItem feedItem = new FeedItem();
                feedItem.setId(flashCard.getId());
                feedItem.setTitle("Flash Card: " + flashCard.getCategory());
                feedItem.setDescription("Q: " + flashCard.getQuestion() + " | A: " + flashCard.getAnswer());
                feedItem.setLink("/api/flashcards/" + flashCard.getId());
                feedItem.setPubDate(flashCard.getCreatedDate());
                feedItem.setType("FLASHCARD");
                feedItem.setCategory(flashCard.getCategory());
                feedItem.setDifficulty(flashCard.getDifficulty());
                feedItems.add(feedItem);
            }
            
            // Sort by creation date (newest first) and limit results
            feedItems.sort((a, b) -> {
                if (a.getPubDate() == null && b.getPubDate() == null) return 0;
                if (a.getPubDate() == null) return 1;
                if (b.getPubDate() == null) return -1;
                return b.getPubDate().compareTo(a.getPubDate());
            });
            
            return feedItems.stream().limit(limit).collect(Collectors.toList());
            
        } catch (Exception e) {
            e.printStackTrace();
            return feedItems;
        }
    }
    
    private List<RSSItem> getRSSItemsFromRedis() {
        List<RSSItem> rssItems = new ArrayList<>();
        try {
            // Get all keys that match RSS item pattern (assuming they don't have a specific prefix)
            // This is a simplified approach - in production you might want to use a specific key pattern
            // For now, we'll return an empty list since we don't have a way to distinguish RSS items from other Redis keys
            // You might want to modify the fetchAndSaveRSS method to use a specific key prefix like "rss:"
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rssItems;
    }

}

