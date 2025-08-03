package com.example.rssFeedv2;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/flashcards")
public class FlashCardController {

    @Autowired
    private FlashCardRepository flashCardRepository;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Create a new flash card
    @PostMapping
    public FlashCard createFlashCard(@RequestBody FlashCard flashCard) {
        try {
            flashCard.setId(UUID.randomUUID().toString());
            FlashCard savedCard = flashCardRepository.save(flashCard);
            
            // Also store in Redis for faster access
            String jsonCard = objectMapper.writeValueAsString(savedCard);
            redisTemplate.opsForValue().set("flashcard:" + savedCard.getId(), jsonCard);
            
            return savedCard;
        } catch (Exception e) {
            throw new RuntimeException("Error creating flash card", e);
        }
    }

    // Get all flash cards
    @GetMapping
    public List<FlashCard> getAllFlashCards() {
        List<FlashCard> flashCards = new ArrayList<>();
        flashCardRepository.findAll().forEach(flashCards::add);
        return flashCards;
    }

    // Get flash card by ID
    @GetMapping("/{id}")
    public FlashCard getFlashCard(@PathVariable String id) {
        return flashCardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Flash card not found with id: " + id));
    }

    // Update flash card
    @PutMapping("/{id}")
    public FlashCard updateFlashCard(@PathVariable String id, @RequestBody FlashCard flashCard) {
        try {
            FlashCard existingCard = flashCardRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Flash card not found with id: " + id));
            
            existingCard.setQuestion(flashCard.getQuestion());
            existingCard.setAnswer(flashCard.getAnswer());
            existingCard.setCategory(flashCard.getCategory());
            existingCard.setDifficulty(flashCard.getDifficulty());
            existingCard.setActive(flashCard.isActive());
            
            FlashCard savedCard = flashCardRepository.save(existingCard);
            
            // Update Redis cache
            String jsonCard = objectMapper.writeValueAsString(savedCard);
            redisTemplate.opsForValue().set("flashcard:" + savedCard.getId(), jsonCard);
            
            return savedCard;
        } catch (Exception e) {
            throw new RuntimeException("Error updating flash card", e);
        }
    }

    // Delete flash card
    @DeleteMapping("/{id}")
    public void deleteFlashCard(@PathVariable String id) {
        flashCardRepository.deleteById(id);
        redisTemplate.delete("flashcard:" + id);
    }

    // Mark flash card as reviewed
    @PostMapping("/{id}/review")
    public FlashCard reviewFlashCard(@PathVariable String id) {
        try {
            FlashCard flashCard = flashCardRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Flash card not found with id: " + id));
            
            flashCard.markAsReviewed();
            FlashCard savedCard = flashCardRepository.save(flashCard);
            
            // Update Redis cache
            String jsonCard = objectMapper.writeValueAsString(savedCard);
            redisTemplate.opsForValue().set("flashcard:" + savedCard.getId(), jsonCard);
            
            return savedCard;
        } catch (Exception e) {
            throw new RuntimeException("Error reviewing flash card", e);
        }
    }

    // Get flash cards by category
    @GetMapping("/category/{category}")
    public List<FlashCard> getFlashCardsByCategory(@PathVariable String category) {
        List<FlashCard> allCards = getAllFlashCards();
        return allCards.stream()
                .filter(card -> category.equalsIgnoreCase(card.getCategory()))
                .filter(FlashCard::isActive)
                .toList();
    }

    // Get flash cards by difficulty
    @GetMapping("/difficulty/{difficulty}")
    public List<FlashCard> getFlashCardsByDifficulty(@PathVariable String difficulty) {
        List<FlashCard> allCards = getAllFlashCards();
        return allCards.stream()
                .filter(card -> difficulty.equalsIgnoreCase(card.getDifficulty()))
                .filter(FlashCard::isActive)
                .toList();
    }

    // Get random flash cards for study session
    @GetMapping("/random")
    public List<FlashCard> getRandomFlashCards(@RequestParam(defaultValue = "10") int count) {
        List<FlashCard> allCards = getAllFlashCards();
        List<FlashCard> activeCards = allCards.stream()
                .filter(FlashCard::isActive)
                .toList();
        
        Collections.shuffle(activeCards);
        return activeCards.stream()
                .limit(count)
                .toList();
    }

    // Bulk create flash cards
    @PostMapping("/bulk")
    public List<FlashCard> createBulkFlashCards(@RequestBody List<FlashCard> flashCards) {
        List<FlashCard> savedCards = new ArrayList<>();
        
        for (FlashCard card : flashCards) {
            try {
                card.setId(UUID.randomUUID().toString());
                FlashCard savedCard = flashCardRepository.save(card);
                
                // Store in Redis
                String jsonCard = objectMapper.writeValueAsString(savedCard);
                redisTemplate.opsForValue().set("flashcard:" + savedCard.getId(), jsonCard);
                
                savedCards.add(savedCard);
            } catch (Exception e) {
                System.err.println("Error saving flash card: " + e.getMessage());
            }
        }
        
        return savedCards;
    }
}