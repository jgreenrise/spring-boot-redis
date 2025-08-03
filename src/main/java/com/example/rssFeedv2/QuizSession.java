package com.example.rssFeedv2;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RedisHash("quiz_sessions")
public class QuizSession implements Serializable {
    @Id
    private String sessionId;
    private String userId;
    private String category;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private List<String> cardIds; // Cards in this session
    private List<Integer> responses; // User responses (0-5 quality scale)
    private int currentCardIndex;
    private int totalCards;
    private int correctAnswers;
    private int incorrectAnswers;
    private boolean isCompleted;
    private int sessionScore; // Overall session score
    private long totalTimeSpent; // In milliseconds
    private String sessionType; // "review", "new", "mixed"

    // Constructors
    public QuizSession() {
        this.cardIds = new ArrayList<>();
        this.responses = new ArrayList<>();
        this.currentCardIndex = 0;
        this.correctAnswers = 0;
        this.incorrectAnswers = 0;
        this.isCompleted = false;
        this.sessionScore = 0;
        this.totalTimeSpent = 0;
        this.startTime = LocalDateTime.now();
    }

    public QuizSession(String userId, String category, String sessionType) {
        this();
        this.userId = userId;
        this.category = category;
        this.sessionType = sessionType;
    }

    // Session management methods
    public void addCard(String cardId) {
        this.cardIds.add(cardId);
        this.totalCards = this.cardIds.size();
    }

    public void recordResponse(int quality) {
        this.responses.add(quality);
        if (quality >= 3) {
            this.correctAnswers++;
        } else {
            this.incorrectAnswers++;
        }
        this.currentCardIndex++;
    }

    public void completeSession() {
        this.isCompleted = true;
        this.endTime = LocalDateTime.now();
        this.totalTimeSpent = java.time.Duration.between(startTime, endTime).toMillis();
        this.calculateSessionScore();
    }

    private void calculateSessionScore() {
        if (totalCards > 0) {
            double accuracy = (double) correctAnswers / totalCards;
            int speedBonus = totalTimeSpent < 300000 ? 10 : 0; // Bonus for completing under 5 minutes
            this.sessionScore = (int) ((accuracy * 100) + speedBonus);
        }
    }

    public String getCurrentCardId() {
        if (currentCardIndex < cardIds.size()) {
            return cardIds.get(currentCardIndex);
        }
        return null;
    }

    public boolean hasMoreCards() {
        return currentCardIndex < cardIds.size();
    }

    public double getAccuracy() {
        return totalCards > 0 ? (double) correctAnswers / totalCards : 0.0;
    }

    public int getProgress() {
        return totalCards > 0 ? (currentCardIndex * 100) / totalCards : 0;
    }

    // Getters and Setters
    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public List<String> getCardIds() {
        return cardIds;
    }

    public void setCardIds(List<String> cardIds) {
        this.cardIds = cardIds;
    }

    public List<Integer> getResponses() {
        return responses;
    }

    public void setResponses(List<Integer> responses) {
        this.responses = responses;
    }

    public int getCurrentCardIndex() {
        return currentCardIndex;
    }

    public void setCurrentCardIndex(int currentCardIndex) {
        this.currentCardIndex = currentCardIndex;
    }

    public int getTotalCards() {
        return totalCards;
    }

    public void setTotalCards(int totalCards) {
        this.totalCards = totalCards;
    }

    public int getCorrectAnswers() {
        return correctAnswers;
    }

    public void setCorrectAnswers(int correctAnswers) {
        this.correctAnswers = correctAnswers;
    }

    public int getIncorrectAnswers() {
        return incorrectAnswers;
    }

    public void setIncorrectAnswers(int incorrectAnswers) {
        this.incorrectAnswers = incorrectAnswers;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public int getSessionScore() {
        return sessionScore;
    }

    public void setSessionScore(int sessionScore) {
        this.sessionScore = sessionScore;
    }

    public long getTotalTimeSpent() {
        return totalTimeSpent;
    }

    public void setTotalTimeSpent(long totalTimeSpent) {
        this.totalTimeSpent = totalTimeSpent;
    }

    public String getSessionType() {
        return sessionType;
    }

    public void setSessionType(String sessionType) {
        this.sessionType = sessionType;
    }
}