package com.example.rssFeedv2;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class QuizService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    public QuizService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.findAndRegisterModules(); // For LocalDateTime support
    }

    // Create a new quiz card
    public QuizCard createCard(String question, String answer, String category, int difficulty) {
        QuizCard card = new QuizCard(question, answer, category, difficulty);
        card.setId(UUID.randomUUID().toString());
        saveCard(card);
        return card;
    }

    // Save card to Redis
    public void saveCard(QuizCard card) {
        try {
            String cardJson = objectMapper.writeValueAsString(card);
            redisTemplate.opsForValue().set("quiz_card:" + card.getId(), cardJson);
            
            // Add to category index
            redisTemplate.opsForSet().add("category:" + card.getCategory(), card.getId());
            
            // Add to all cards index
            redisTemplate.opsForSet().add("all_quiz_cards", card.getId());
        } catch (Exception e) {
            throw new RuntimeException("Failed to save quiz card", e);
        }
    }

    // Get card by ID
    public QuizCard getCard(String cardId) {
        try {
            String cardJson = redisTemplate.opsForValue().get("quiz_card:" + cardId);
            if (cardJson != null) {
                return objectMapper.readValue(cardJson, QuizCard.class);
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve quiz card", e);
        }
    }

    // Get cards due for review (Anki-style spaced repetition)
    public List<QuizCard> getCardsDueForReview(String category, int limit) {
        Set<String> cardIds;
        if (category != null && !category.isEmpty()) {
            cardIds = redisTemplate.opsForSet().members("category:" + category);
        } else {
            cardIds = redisTemplate.opsForSet().members("all_quiz_cards");
        }

        if (cardIds == null) return new ArrayList<>();

        List<QuizCard> dueCards = new ArrayList<>();
        for (String cardId : cardIds) {
            QuizCard card = getCard(cardId);
            if (card != null && card.isActive() && card.isDueForReview()) {
                dueCards.add(card);
            }
        }

        // Sort by priority: newest cards first, then by next review date
        dueCards.sort((a, b) -> {
            if (a.getRepetitions() == 0 && b.getRepetitions() > 0) return -1;
            if (b.getRepetitions() == 0 && a.getRepetitions() > 0) return 1;
            return a.getNextReview().compareTo(b.getNextReview());
        });

        return dueCards.stream().limit(limit).collect(Collectors.toList());
    }

    // Start a new quiz session
    public QuizSession startQuizSession(String userId, String category, String sessionType, int maxCards) {
        List<QuizCard> cards;
        
        switch (sessionType.toLowerCase()) {
            case "review":
                cards = getCardsDueForReview(category, maxCards);
                break;
            case "new":
                cards = getNewCards(category, maxCards);
                break;
            case "mixed":
            default:
                List<QuizCard> reviewCards = getCardsDueForReview(category, maxCards / 2);
                List<QuizCard> newCards = getNewCards(category, maxCards - reviewCards.size());
                cards = new ArrayList<>(reviewCards);
                cards.addAll(newCards);
                Collections.shuffle(cards);
                break;
        }

        QuizSession session = new QuizSession(userId, category, sessionType);
        session.setSessionId(UUID.randomUUID().toString());
        
        for (QuizCard card : cards) {
            session.addCard(card.getId());
        }

        saveSession(session);
        return session;
    }

    // Get new cards (never studied)
    public List<QuizCard> getNewCards(String category, int limit) {
        Set<String> cardIds;
        if (category != null && !category.isEmpty()) {
            cardIds = redisTemplate.opsForSet().members("category:" + category);
        } else {
            cardIds = redisTemplate.opsForSet().members("all_quiz_cards");
        }

        if (cardIds == null) return new ArrayList<>();

        List<QuizCard> newCards = new ArrayList<>();
        for (String cardId : cardIds) {
            QuizCard card = getCard(cardId);
            if (card != null && card.isActive() && card.getRepetitions() == 0) {
                newCards.add(card);
            }
        }

        Collections.shuffle(newCards);
        return newCards.stream().limit(limit).collect(Collectors.toList());
    }

    // Save quiz session
    public void saveSession(QuizSession session) {
        try {
            String sessionJson = objectMapper.writeValueAsString(session);
            redisTemplate.opsForValue().set("quiz_session:" + session.getSessionId(), sessionJson);
            
            // Add to user's sessions index
            redisTemplate.opsForSet().add("user_sessions:" + session.getUserId(), session.getSessionId());
        } catch (Exception e) {
            throw new RuntimeException("Failed to save quiz session", e);
        }
    }

    // Get quiz session
    public QuizSession getSession(String sessionId) {
        try {
            String sessionJson = redisTemplate.opsForValue().get("quiz_session:" + sessionId);
            if (sessionJson != null) {
                return objectMapper.readValue(sessionJson, QuizSession.class);
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve quiz session", e);
        }
    }

    // Submit answer and update card using spaced repetition
    public QuizCard submitAnswer(String sessionId, int quality) {
        QuizSession session = getSession(sessionId);
        if (session == null || !session.hasMoreCards()) {
            throw new RuntimeException("Invalid session or no more cards");
        }

        String currentCardId = session.getCurrentCardId();
        QuizCard card = getCard(currentCardId);
        
        if (card == null) {
            throw new RuntimeException("Card not found");
        }

        // Update card using Anki spaced repetition algorithm
        card.updateSpacedRepetition(quality);
        saveCard(card);

        // Record response in session
        session.recordResponse(quality);
        
        // Check if session is complete
        if (!session.hasMoreCards()) {
            session.completeSession();
            
            // Update leaderboard
            updateLeaderboard(session.getUserId(), session.getSessionScore());
        }
        
        saveSession(session);
        return card;
    }

    // Update leaderboard with session score
    private void updateLeaderboard(String userId, int score) {
        String leaderboardKey = "quiz_leaderboard:" + LocalDateTime.now().getYear() + 
                               "-" + LocalDateTime.now().getMonthValue();
        redisTemplate.opsForZSet().incrementScore(leaderboardKey, userId, score);
    }

    // Get current card in session
    public QuizCard getCurrentCard(String sessionId) {
        QuizSession session = getSession(sessionId);
        if (session == null || !session.hasMoreCards()) {
            return null;
        }
        
        return getCard(session.getCurrentCardId());
    }

    // Get all categories
    public Set<String> getAllCategories() {
        Set<String> categories = new HashSet<>();
        Set<String> keys = redisTemplate.keys("category:*");
        if (keys != null) {
            for (String key : keys) {
                categories.add(key.substring("category:".length()));
            }
        }
        return categories;
    }

    // Get user statistics
    public Map<String, Object> getUserStats(String userId) {
        Map<String, Object> stats = new HashMap<>();
        
        // Get user sessions
        Set<String> sessionIds = redisTemplate.opsForSet().members("user_sessions:" + userId);
        int totalSessions = sessionIds != null ? sessionIds.size() : 0;
        
        int totalCards = 0;
        int totalCorrect = 0;
        long totalTime = 0;
        
        if (sessionIds != null) {
            for (String sessionId : sessionIds) {
                QuizSession session = getSession(sessionId);
                if (session != null && session.isCompleted()) {
                    totalCards += session.getTotalCards();
                    totalCorrect += session.getCorrectAnswers();
                    totalTime += session.getTotalTimeSpent();
                }
            }
        }
        
        stats.put("totalSessions", totalSessions);
        stats.put("totalCards", totalCards);
        stats.put("totalCorrect", totalCorrect);
        stats.put("accuracy", totalCards > 0 ? (double) totalCorrect / totalCards : 0.0);
        stats.put("totalTimeSpent", totalTime);
        stats.put("averageSessionTime", totalSessions > 0 ? totalTime / totalSessions : 0);
        
        return stats;
    }

    // Delete a card
    public boolean deleteCard(String cardId) {
        QuizCard card = getCard(cardId);
        if (card == null) return false;
        
        // Remove from Redis
        redisTemplate.delete("quiz_card:" + cardId);
        redisTemplate.opsForSet().remove("category:" + card.getCategory(), cardId);
        redisTemplate.opsForSet().remove("all_quiz_cards", cardId);
        
        return true;
    }

    // Update card
    public QuizCard updateCard(String cardId, String question, String answer, String category, int difficulty) {
        QuizCard card = getCard(cardId);
        if (card == null) return null;
        
        // Remove from old category if changed
        if (!card.getCategory().equals(category)) {
            redisTemplate.opsForSet().remove("category:" + card.getCategory(), cardId);
        }
        
        card.setQuestion(question);
        card.setAnswer(answer);
        card.setCategory(category);
        card.setDifficulty(difficulty);
        
        saveCard(card);
        return card;
    }
}