package com.example.rssFeedv2;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/flashcards")
@CrossOrigin(origins = "*")
public class FlashCardController {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private static final String FLASHCARD_KEY_PREFIX = "flashcard:";
    private static final String FLASHCARD_LIST_KEY = "flashcards:all";

    public FlashCardController(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = new ObjectMapper();
    }

    @PostMapping
    public ResponseEntity<FlashCard> createFlashCard(@RequestBody FlashCard flashCard) {
        try {
            // Generate UUID for the flash card
            String id = UUID.randomUUID().toString();
            flashCard.setId(id);
            flashCard.setCreatedDate(new Date());
            
            // Convert to JSON and store in Redis
            String jsonFlashCard = objectMapper.writeValueAsString(flashCard);
            String key = FLASHCARD_KEY_PREFIX + id;
            
            redisTemplate.opsForValue().set(key, jsonFlashCard);
            // Add to list of all flash cards
            redisTemplate.opsForList().leftPush(FLASHCARD_LIST_KEY, id);
            
            return ResponseEntity.ok(flashCard);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<FlashCard>> getAllFlashCards() {
        try {
            List<String> flashCardIds = redisTemplate.opsForList().range(FLASHCARD_LIST_KEY, 0, -1);
            List<FlashCard> flashCards = new ArrayList<>();
            
            if (flashCardIds != null) {
                for (String id : flashCardIds) {
                    String key = FLASHCARD_KEY_PREFIX + id;
                    String jsonFlashCard = redisTemplate.opsForValue().get(key);
                    if (jsonFlashCard != null) {
                        FlashCard flashCard = objectMapper.readValue(jsonFlashCard, FlashCard.class);
                        flashCards.add(flashCard);
                    }
                }
            }
            
            // Sort by creation date (newest first)
            flashCards.sort((a, b) -> b.getCreatedDate().compareTo(a.getCreatedDate()));
            
            return ResponseEntity.ok(flashCards);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<FlashCard> getFlashCard(@PathVariable String id) {
        try {
            String key = FLASHCARD_KEY_PREFIX + id;
            String jsonFlashCard = redisTemplate.opsForValue().get(key);
            
            if (jsonFlashCard == null) {
                return ResponseEntity.notFound().build();
            }
            
            FlashCard flashCard = objectMapper.readValue(jsonFlashCard, FlashCard.class);
            return ResponseEntity.ok(flashCard);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<FlashCard> updateFlashCard(@PathVariable String id, @RequestBody FlashCard updatedFlashCard) {
        try {
            String key = FLASHCARD_KEY_PREFIX + id;
            String existingJson = redisTemplate.opsForValue().get(key);
            
            if (existingJson == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Keep the original ID and creation date
            updatedFlashCard.setId(id);
            FlashCard existing = objectMapper.readValue(existingJson, FlashCard.class);
            updatedFlashCard.setCreatedDate(existing.getCreatedDate());
            
            String jsonFlashCard = objectMapper.writeValueAsString(updatedFlashCard);
            redisTemplate.opsForValue().set(key, jsonFlashCard);
            
            return ResponseEntity.ok(updatedFlashCard);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFlashCard(@PathVariable String id) {
        try {
            String key = FLASHCARD_KEY_PREFIX + id;
            Boolean deleted = redisTemplate.delete(key);
            
            if (deleted != null && deleted) {
                // Remove from the list as well
                redisTemplate.opsForList().remove(FLASHCARD_LIST_KEY, 1, id);
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{id}/review")
    public ResponseEntity<FlashCard> markAsReviewed(@PathVariable String id) {
        try {
            String key = FLASHCARD_KEY_PREFIX + id;
            String jsonFlashCard = redisTemplate.opsForValue().get(key);
            
            if (jsonFlashCard == null) {
                return ResponseEntity.notFound().build();
            }
            
            FlashCard flashCard = objectMapper.readValue(jsonFlashCard, FlashCard.class);
            flashCard.setLastReviewed(new Date());
            flashCard.setReviewCount(flashCard.getReviewCount() + 1);
            
            String updatedJson = objectMapper.writeValueAsString(flashCard);
            redisTemplate.opsForValue().set(key, updatedJson);
            
            return ResponseEntity.ok(flashCard);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
}