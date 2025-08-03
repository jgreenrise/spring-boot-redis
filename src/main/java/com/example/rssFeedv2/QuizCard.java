package com.example.rssFeedv2;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import java.io.Serializable;
import java.time.LocalDateTime;

@RedisHash("quiz_cards")
public class QuizCard implements Serializable {
    @Id
    private String id;
    private String question;  // Front of the card
    private String answer;    // Back of the card
    private String category;  // Subject category
    private int difficulty;   // 1 (easy) to 5 (hard)
    private int repetitions;  // Number of times reviewed
    private double easinessFactor; // Anki-style easiness factor (1.3 - 2.5)
    private int interval;     // Days until next review
    private LocalDateTime nextReview; // When to show this card next
    private LocalDateTime lastReviewed;
    private int correctStreak; // Consecutive correct answers
    private int totalAttempts;
    private int correctAttempts;
    private boolean isActive; // Whether card is in active rotation

    // Constructors
    public QuizCard() {
        this.easinessFactor = 2.5; // Default Anki easiness
        this.interval = 1;
        this.repetitions = 0;
        this.correctStreak = 0;
        this.totalAttempts = 0;
        this.correctAttempts = 0;
        this.isActive = true;
        this.nextReview = LocalDateTime.now();
    }

    public QuizCard(String question, String answer, String category, int difficulty) {
        this();
        this.question = question;
        this.answer = answer;
        this.category = category;
        this.difficulty = difficulty;
    }

    // Anki-style spaced repetition algorithm
    public void updateSpacedRepetition(int quality) {
        // quality: 0-5 (0=complete blackout, 5=perfect response)
        this.totalAttempts++;
        this.lastReviewed = LocalDateTime.now();
        
        if (quality >= 3) { // Correct answer
            this.correctAttempts++;
            this.correctStreak++;
            
            if (this.repetitions == 0) {
                this.interval = 1;
            } else if (this.repetitions == 1) {
                this.interval = 6;
            } else {
                this.interval = (int) Math.round(this.interval * this.easinessFactor);
            }
            this.repetitions++;
        } else { // Incorrect answer
            this.correctStreak = 0;
            this.repetitions = 0;
            this.interval = 1;
        }
        
        // Update easiness factor (Anki algorithm)
        this.easinessFactor = this.easinessFactor + (0.1 - (5 - quality) * (0.08 + (5 - quality) * 0.02));
        if (this.easinessFactor < 1.3) {
            this.easinessFactor = 1.3;
        }
        
        // Set next review date
        this.nextReview = LocalDateTime.now().plusDays(this.interval);
    }

    public boolean isDueForReview() {
        return LocalDateTime.now().isAfter(this.nextReview);
    }

    public double getSuccessRate() {
        return totalAttempts > 0 ? (double) correctAttempts / totalAttempts : 0.0;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }

    public int getRepetitions() {
        return repetitions;
    }

    public void setRepetitions(int repetitions) {
        this.repetitions = repetitions;
    }

    public double getEasinessFactor() {
        return easinessFactor;
    }

    public void setEasinessFactor(double easinessFactor) {
        this.easinessFactor = easinessFactor;
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public LocalDateTime getNextReview() {
        return nextReview;
    }

    public void setNextReview(LocalDateTime nextReview) {
        this.nextReview = nextReview;
    }

    public LocalDateTime getLastReviewed() {
        return lastReviewed;
    }

    public void setLastReviewed(LocalDateTime lastReviewed) {
        this.lastReviewed = lastReviewed;
    }

    public int getCorrectStreak() {
        return correctStreak;
    }

    public void setCorrectStreak(int correctStreak) {
        this.correctStreak = correctStreak;
    }

    public int getTotalAttempts() {
        return totalAttempts;
    }

    public void setTotalAttempts(int totalAttempts) {
        this.totalAttempts = totalAttempts;
    }

    public int getCorrectAttempts() {
        return correctAttempts;
    }

    public void setCorrectAttempts(int correctAttempts) {
        this.correctAttempts = correctAttempts;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}