package com.example.rssFeedv2;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/flashcards")
public class FlashCardController {

    private final FlashCardService flashCardService;

    public FlashCardController(FlashCardService flashCardService) {
        this.flashCardService = flashCardService;
    }

    /**
     * Create a new flash card
     * POST /api/flashcards
     */
    @PostMapping
    public ResponseEntity<FlashCard> createFlashCard(@RequestBody FlashCard flashCard) {
        try {
            // Validate required fields
            if (flashCard.getQuestion() == null || flashCard.getQuestion().trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            if (flashCard.getAnswer() == null || flashCard.getAnswer().trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            FlashCard createdFlashCard = flashCardService.createFlashCard(flashCard);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdFlashCard);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get a flash card by ID
     * GET /api/flashcards/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<FlashCard> getFlashCard(@PathVariable String id) {
        try {
            Optional<FlashCard> flashCard = flashCardService.getFlashCard(id);
            return flashCard.map(card -> ResponseEntity.ok().body(card))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get all flash cards
     * GET /api/flashcards
     */
    @GetMapping
    public ResponseEntity<List<FlashCard>> getAllFlashCards() {
        try {
            List<FlashCard> flashCards = flashCardService.getAllFlashCards();
            return ResponseEntity.ok().body(flashCards);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get flash cards by category
     * GET /api/flashcards/category/{category}
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<List<FlashCard>> getFlashCardsByCategory(@PathVariable String category) {
        try {
            List<FlashCard> flashCards = flashCardService.getFlashCardsByCategory(category);
            return ResponseEntity.ok().body(flashCards);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get flash cards by difficulty
     * GET /api/flashcards/difficulty/{difficulty}
     */
    @GetMapping("/difficulty/{difficulty}")
    public ResponseEntity<List<FlashCard>> getFlashCardsByDifficulty(@PathVariable String difficulty) {
        try {
            List<FlashCard> flashCards = flashCardService.getFlashCardsByDifficulty(difficulty);
            return ResponseEntity.ok().body(flashCards);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Update a flash card
     * PUT /api/flashcards/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<FlashCard> updateFlashCard(@PathVariable String id, @RequestBody FlashCard flashCard) {
        try {
            // Validate required fields
            if (flashCard.getQuestion() == null || flashCard.getQuestion().trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            if (flashCard.getAnswer() == null || flashCard.getAnswer().trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            Optional<FlashCard> updatedFlashCard = flashCardService.updateFlashCard(id, flashCard);
            return updatedFlashCard.map(card -> ResponseEntity.ok().body(card))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Delete a flash card
     * DELETE /api/flashcards/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFlashCard(@PathVariable String id) {
        try {
            boolean deleted = flashCardService.deleteFlashCard(id);
            return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get flash card count
     * GET /api/flashcards/count
     */
    @GetMapping("/count")
    public ResponseEntity<Long> getFlashCardCount() {
        try {
            long count = flashCardService.getFlashCardCount();
            return ResponseEntity.ok().body(count);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Delete all flash cards
     * DELETE /api/flashcards
     */
    @DeleteMapping
    public ResponseEntity<Void> deleteAllFlashCards() {
        try {
            flashCardService.deleteAllFlashCards();
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get a random flash card
     * GET /api/flashcards/random
     */
    @GetMapping("/random")
    public ResponseEntity<FlashCard> getRandomFlashCard() {
        try {
            List<FlashCard> allFlashCards = flashCardService.getAllFlashCards();
            
            if (allFlashCards.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            int randomIndex = (int) (Math.random() * allFlashCards.size());
            FlashCard randomFlashCard = allFlashCards.get(randomIndex);
            
            return ResponseEntity.ok().body(randomFlashCard);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}