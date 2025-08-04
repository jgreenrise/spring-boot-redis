package com.example.rssFeedv2;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class FlashCardService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private static final String FLASHCARD_KEY_PREFIX = "flashcard:";
    private static final String FLASHCARD_SET_KEY = "flashcards:all";

    public FlashCardService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    public FlashCard createFlashCard(FlashCard flashCard) {
        try {
            String id = UUID.randomUUID().toString();
            flashCard.setId(id);
            
            String jsonFlashCard = objectMapper.writeValueAsString(flashCard);
            String key = FLASHCARD_KEY_PREFIX + id;
            
            // Store the flash card
            redisTemplate.opsForValue().set(key, jsonFlashCard);
            
            // Add to the set of all flash cards for easy retrieval
            redisTemplate.opsForSet().add(FLASHCARD_SET_KEY, id);
            
            return flashCard;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error creating flash card", e);
        }
    }

    public Optional<FlashCard> getFlashCard(String id) {
        try {
            String key = FLASHCARD_KEY_PREFIX + id;
            String jsonFlashCard = redisTemplate.opsForValue().get(key);
            
            if (jsonFlashCard == null) {
                return Optional.empty();
            }
            
            FlashCard flashCard = objectMapper.readValue(jsonFlashCard, FlashCard.class);
            return Optional.of(flashCard);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error retrieving flash card", e);
        }
    }

    public List<FlashCard> getAllFlashCards() {
        try {
            Set<String> flashCardIds = redisTemplate.opsForSet().members(FLASHCARD_SET_KEY);
            
            if (flashCardIds == null || flashCardIds.isEmpty()) {
                return new ArrayList<>();
            }
            
            return flashCardIds.stream()
                    .map(this::getFlashCard)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Error retrieving all flash cards", e);
        }
    }

    public List<FlashCard> getFlashCardsByCategory(String category) {
        return getAllFlashCards().stream()
                .filter(flashCard -> category.equalsIgnoreCase(flashCard.getCategory()))
                .collect(Collectors.toList());
    }

    public List<FlashCard> getFlashCardsByDifficulty(String difficulty) {
        return getAllFlashCards().stream()
                .filter(flashCard -> difficulty.equalsIgnoreCase(flashCard.getDifficulty()))
                .collect(Collectors.toList());
    }

    public Optional<FlashCard> updateFlashCard(String id, FlashCard updatedFlashCard) {
        try {
            Optional<FlashCard> existingFlashCard = getFlashCard(id);
            
            if (existingFlashCard.isEmpty()) {
                return Optional.empty();
            }
            
            // Update the flash card while preserving the original ID and creation date
            updatedFlashCard.setId(id);
            updatedFlashCard.setCreatedAt(existingFlashCard.get().getCreatedAt());
            
            String jsonFlashCard = objectMapper.writeValueAsString(updatedFlashCard);
            String key = FLASHCARD_KEY_PREFIX + id;
            
            redisTemplate.opsForValue().set(key, jsonFlashCard);
            
            return Optional.of(updatedFlashCard);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error updating flash card", e);
        }
    }

    public boolean deleteFlashCard(String id) {
        String key = FLASHCARD_KEY_PREFIX + id;
        
        // Check if the flash card exists
        if (!redisTemplate.hasKey(key)) {
            return false;
        }
        
        // Remove from the main storage
        redisTemplate.delete(key);
        
        // Remove from the set of all flash cards
        redisTemplate.opsForSet().remove(FLASHCARD_SET_KEY, id);
        
        return true;
    }

    public long getFlashCardCount() {
        Long count = redisTemplate.opsForSet().size(FLASHCARD_SET_KEY);
        return count != null ? count : 0;
    }

    public void deleteAllFlashCards() {
        Set<String> flashCardIds = redisTemplate.opsForSet().members(FLASHCARD_SET_KEY);
        
        if (flashCardIds != null && !flashCardIds.isEmpty()) {
            // Delete all individual flash card keys
            List<String> keysToDelete = flashCardIds.stream()
                    .map(id -> FLASHCARD_KEY_PREFIX + id)
                    .collect(Collectors.toList());
            
            redisTemplate.delete(keysToDelete);
        }
        
        // Delete the set containing all flash card IDs
        redisTemplate.delete(FLASHCARD_SET_KEY);
    }
}