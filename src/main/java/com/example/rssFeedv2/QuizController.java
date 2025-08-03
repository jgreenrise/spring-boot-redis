package com.example.rssFeedv2;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/quiz")
public class QuizController {

    private final QuizService quizService;

    public QuizController(QuizService quizService) {
        this.quizService = quizService;
    }

    // ===== CARD MANAGEMENT =====

    /**
     * Create a new quiz card
     * POST /api/quiz/cards
     */
    @PostMapping("/cards")
    public ResponseEntity<QuizCard> createCard(@RequestBody CreateCardRequest request) {
        try {
            QuizCard card = quizService.createCard(
                request.getQuestion(),
                request.getAnswer(),
                request.getCategory(),
                request.getDifficulty()
            );
            return ResponseEntity.ok(card);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get a specific card by ID
     * GET /api/quiz/cards/{cardId}
     */
    @GetMapping("/cards/{cardId}")
    public ResponseEntity<QuizCard> getCard(@PathVariable String cardId) {
        QuizCard card = quizService.getCard(cardId);
        if (card != null) {
            return ResponseEntity.ok(card);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Update an existing card
     * PUT /api/quiz/cards/{cardId}
     */
    @PutMapping("/cards/{cardId}")
    public ResponseEntity<QuizCard> updateCard(@PathVariable String cardId, @RequestBody CreateCardRequest request) {
        QuizCard card = quizService.updateCard(
            cardId,
            request.getQuestion(),
            request.getAnswer(),
            request.getCategory(),
            request.getDifficulty()
        );
        if (card != null) {
            return ResponseEntity.ok(card);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Delete a card
     * DELETE /api/quiz/cards/{cardId}
     */
    @DeleteMapping("/cards/{cardId}")
    public ResponseEntity<Void> deleteCard(@PathVariable String cardId) {
        boolean deleted = quizService.deleteCard(cardId);
        if (deleted) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Get cards due for review
     * GET /api/quiz/cards/due?category=math&limit=20
     */
    @GetMapping("/cards/due")
    public ResponseEntity<List<QuizCard>> getCardsDue(
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "20") int limit) {
        List<QuizCard> cards = quizService.getCardsDueForReview(category, limit);
        return ResponseEntity.ok(cards);
    }

    /**
     * Get new cards (never studied)
     * GET /api/quiz/cards/new?category=math&limit=10
     */
    @GetMapping("/cards/new")
    public ResponseEntity<List<QuizCard>> getNewCards(
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "10") int limit) {
        List<QuizCard> cards = quizService.getNewCards(category, limit);
        return ResponseEntity.ok(cards);
    }

    // ===== QUIZ SESSIONS =====

    /**
     * Start a new quiz session
     * POST /api/quiz/sessions/start
     */
    @PostMapping("/sessions/start")
    public ResponseEntity<SessionResponse> startSession(@RequestBody StartSessionRequest request) {
        try {
            QuizSession session = quizService.startQuizSession(
                request.getUserId(),
                request.getCategory(),
                request.getSessionType(),
                request.getMaxCards()
            );
            
            SessionResponse response = new SessionResponse();
            response.setSessionId(session.getSessionId());
            response.setTotalCards(session.getTotalCards());
            response.setCurrentCardIndex(session.getCurrentCardIndex());
            response.setProgress(session.getProgress());
            
            // Include first card if available
            QuizCard currentCard = quizService.getCurrentCard(session.getSessionId());
            if (currentCard != null) {
                response.setCurrentCard(currentCard);
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get current card in session
     * GET /api/quiz/sessions/{sessionId}/current
     */
    @GetMapping("/sessions/{sessionId}/current")
    public ResponseEntity<CardResponse> getCurrentCard(@PathVariable String sessionId) {
        QuizSession session = quizService.getSession(sessionId);
        if (session == null) {
            return ResponseEntity.notFound().build();
        }

        QuizCard card = quizService.getCurrentCard(sessionId);
        if (card == null) {
            return ResponseEntity.notFound().build();
        }

        CardResponse response = new CardResponse();
        response.setCard(card);
        response.setProgress(session.getProgress());
        response.setCurrentCardIndex(session.getCurrentCardIndex());
        response.setTotalCards(session.getTotalCards());
        response.setShowAnswer(false); // Initially hide answer

        return ResponseEntity.ok(response);
    }

    /**
     * Submit answer for current card (Anki-style quality rating)
     * POST /api/quiz/sessions/{sessionId}/answer
     * Quality scale: 0=blackout, 1=incorrect, 2=incorrect but remembered, 3=correct with difficulty, 4=correct, 5=perfect
     */
    @PostMapping("/sessions/{sessionId}/answer")
    public ResponseEntity<AnswerResponse> submitAnswer(
            @PathVariable String sessionId,
            @RequestBody SubmitAnswerRequest request) {
        try {
            QuizCard updatedCard = quizService.submitAnswer(sessionId, request.getQuality());
            QuizSession session = quizService.getSession(sessionId);
            
            AnswerResponse response = new AnswerResponse();
            response.setCard(updatedCard);
            response.setCorrect(request.getQuality() >= 3);
            response.setNextInterval(updatedCard.getInterval());
            response.setProgress(session.getProgress());
            response.setSessionCompleted(!session.hasMoreCards());
            
            if (response.isSessionCompleted()) {
                response.setSessionScore(session.getSessionScore());
                response.setAccuracy(session.getAccuracy());
                response.setTotalTimeSpent(session.getTotalTimeSpent());
            } else {
                // Get next card
                QuizCard nextCard = quizService.getCurrentCard(sessionId);
                response.setNextCard(nextCard);
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get session status
     * GET /api/quiz/sessions/{sessionId}/status
     */
    @GetMapping("/sessions/{sessionId}/status")
    public ResponseEntity<SessionStatusResponse> getSessionStatus(@PathVariable String sessionId) {
        QuizSession session = quizService.getSession(sessionId);
        if (session == null) {
            return ResponseEntity.notFound().build();
        }

        SessionStatusResponse response = new SessionStatusResponse();
        response.setSessionId(session.getSessionId());
        response.setUserId(session.getUserId());
        response.setCategory(session.getCategory());
        response.setSessionType(session.getSessionType());
        response.setProgress(session.getProgress());
        response.setCurrentCardIndex(session.getCurrentCardIndex());
        response.setTotalCards(session.getTotalCards());
        response.setCorrectAnswers(session.getCorrectAnswers());
        response.setIncorrectAnswers(session.getIncorrectAnswers());
        response.setCompleted(session.isCompleted());
        response.setAccuracy(session.getAccuracy());
        
        if (session.isCompleted()) {
            response.setSessionScore(session.getSessionScore());
            response.setTotalTimeSpent(session.getTotalTimeSpent());
        }

        return ResponseEntity.ok(response);
    }

    // ===== STATISTICS & ANALYTICS =====

    /**
     * Get user statistics
     * GET /api/quiz/users/{userId}/stats
     */
    @GetMapping("/users/{userId}/stats")
    public ResponseEntity<Map<String, Object>> getUserStats(@PathVariable String userId) {
        Map<String, Object> stats = quizService.getUserStats(userId);
        return ResponseEntity.ok(stats);
    }

    /**
     * Get all categories
     * GET /api/quiz/categories
     */
    @GetMapping("/categories")
    public ResponseEntity<Set<String>> getCategories() {
        Set<String> categories = quizService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    /**
     * Get leaderboard
     * GET /api/quiz/leaderboard?limit=10
     */
    @GetMapping("/leaderboard")
    public ResponseEntity<List<LeaderboardEntry>> getLeaderboard(@RequestParam(defaultValue = "10") int limit) {
        // This would typically get the current month's leaderboard
        // Implementation would use the existing SortedSetController logic
        return ResponseEntity.ok(new ArrayList<>());
    }

    // ===== REQUEST/RESPONSE CLASSES =====

    public static class CreateCardRequest {
        private String question;
        private String answer;
        private String category;
        private int difficulty;

        // Getters and setters
        public String getQuestion() { return question; }
        public void setQuestion(String question) { this.question = question; }
        public String getAnswer() { return answer; }
        public void setAnswer(String answer) { this.answer = answer; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public int getDifficulty() { return difficulty; }
        public void setDifficulty(int difficulty) { this.difficulty = difficulty; }
    }

    public static class StartSessionRequest {
        private String userId;
        private String category;
        private String sessionType; // "review", "new", "mixed"
        private int maxCards;

        // Getters and setters
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public String getSessionType() { return sessionType; }
        public void setSessionType(String sessionType) { this.sessionType = sessionType; }
        public int getMaxCards() { return maxCards; }
        public void setMaxCards(int maxCards) { this.maxCards = maxCards; }
    }

    public static class SubmitAnswerRequest {
        private int quality; // 0-5 Anki quality scale

        public int getQuality() { return quality; }
        public void setQuality(int quality) { this.quality = quality; }
    }

    public static class SessionResponse {
        private String sessionId;
        private int totalCards;
        private int currentCardIndex;
        private int progress;
        private QuizCard currentCard;

        // Getters and setters
        public String getSessionId() { return sessionId; }
        public void setSessionId(String sessionId) { this.sessionId = sessionId; }
        public int getTotalCards() { return totalCards; }
        public void setTotalCards(int totalCards) { this.totalCards = totalCards; }
        public int getCurrentCardIndex() { return currentCardIndex; }
        public void setCurrentCardIndex(int currentCardIndex) { this.currentCardIndex = currentCardIndex; }
        public int getProgress() { return progress; }
        public void setProgress(int progress) { this.progress = progress; }
        public QuizCard getCurrentCard() { return currentCard; }
        public void setCurrentCard(QuizCard currentCard) { this.currentCard = currentCard; }
    }

    public static class CardResponse {
        private QuizCard card;
        private int progress;
        private int currentCardIndex;
        private int totalCards;
        private boolean showAnswer;

        // Getters and setters
        public QuizCard getCard() { return card; }
        public void setCard(QuizCard card) { this.card = card; }
        public int getProgress() { return progress; }
        public void setProgress(int progress) { this.progress = progress; }
        public int getCurrentCardIndex() { return currentCardIndex; }
        public void setCurrentCardIndex(int currentCardIndex) { this.currentCardIndex = currentCardIndex; }
        public int getTotalCards() { return totalCards; }
        public void setTotalCards(int totalCards) { this.totalCards = totalCards; }
        public boolean isShowAnswer() { return showAnswer; }
        public void setShowAnswer(boolean showAnswer) { this.showAnswer = showAnswer; }
    }

    public static class AnswerResponse {
        private QuizCard card;
        private boolean correct;
        private int nextInterval;
        private int progress;
        private boolean sessionCompleted;
        private int sessionScore;
        private double accuracy;
        private long totalTimeSpent;
        private QuizCard nextCard;

        // Getters and setters
        public QuizCard getCard() { return card; }
        public void setCard(QuizCard card) { this.card = card; }
        public boolean isCorrect() { return correct; }
        public void setCorrect(boolean correct) { this.correct = correct; }
        public int getNextInterval() { return nextInterval; }
        public void setNextInterval(int nextInterval) { this.nextInterval = nextInterval; }
        public int getProgress() { return progress; }
        public void setProgress(int progress) { this.progress = progress; }
        public boolean isSessionCompleted() { return sessionCompleted; }
        public void setSessionCompleted(boolean sessionCompleted) { this.sessionCompleted = sessionCompleted; }
        public int getSessionScore() { return sessionScore; }
        public void setSessionScore(int sessionScore) { this.sessionScore = sessionScore; }
        public double getAccuracy() { return accuracy; }
        public void setAccuracy(double accuracy) { this.accuracy = accuracy; }
        public long getTotalTimeSpent() { return totalTimeSpent; }
        public void setTotalTimeSpent(long totalTimeSpent) { this.totalTimeSpent = totalTimeSpent; }
        public QuizCard getNextCard() { return nextCard; }
        public void setNextCard(QuizCard nextCard) { this.nextCard = nextCard; }
    }

    public static class SessionStatusResponse {
        private String sessionId;
        private String userId;
        private String category;
        private String sessionType;
        private int progress;
        private int currentCardIndex;
        private int totalCards;
        private int correctAnswers;
        private int incorrectAnswers;
        private boolean completed;
        private double accuracy;
        private int sessionScore;
        private long totalTimeSpent;

        // Getters and setters
        public String getSessionId() { return sessionId; }
        public void setSessionId(String sessionId) { this.sessionId = sessionId; }
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public String getSessionType() { return sessionType; }
        public void setSessionType(String sessionType) { this.sessionType = sessionType; }
        public int getProgress() { return progress; }
        public void setProgress(int progress) { this.progress = progress; }
        public int getCurrentCardIndex() { return currentCardIndex; }
        public void setCurrentCardIndex(int currentCardIndex) { this.currentCardIndex = currentCardIndex; }
        public int getTotalCards() { return totalCards; }
        public void setTotalCards(int totalCards) { this.totalCards = totalCards; }
        public int getCorrectAnswers() { return correctAnswers; }
        public void setCorrectAnswers(int correctAnswers) { this.correctAnswers = correctAnswers; }
        public int getIncorrectAnswers() { return incorrectAnswers; }
        public void setIncorrectAnswers(int incorrectAnswers) { this.incorrectAnswers = incorrectAnswers; }
        public boolean isCompleted() { return completed; }
        public void setCompleted(boolean completed) { this.completed = completed; }
        public double getAccuracy() { return accuracy; }
        public void setAccuracy(double accuracy) { this.accuracy = accuracy; }
        public int getSessionScore() { return sessionScore; }
        public void setSessionScore(int sessionScore) { this.sessionScore = sessionScore; }
        public long getTotalTimeSpent() { return totalTimeSpent; }
        public void setTotalTimeSpent(long totalTimeSpent) { this.totalTimeSpent = totalTimeSpent; }
    }

    public static class LeaderboardEntry {
        private String userId;
        private int score;
        private int rank;

        // Getters and setters
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public int getScore() { return score; }
        public void setScore(int score) { this.score = score; }
        public int getRank() { return rank; }
        public void setRank(int rank) { this.rank = rank; }
    }
}