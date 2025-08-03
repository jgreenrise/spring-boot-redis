package com.example.rssFeedv2;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import java.io.Serializable;
import java.util.Date;

@RedisHash
public class FlashCard implements Serializable {
    @Id
    private String id;
    private String question;
    private String answer;
    private String category;
    private String difficulty; // EASY, MEDIUM, HARD
    private Date createdDate;
    private Date lastReviewed;
    private int reviewCount;
    private boolean isActive;

    // Default constructor
    public FlashCard() {
        this.createdDate = new Date();
        this.reviewCount = 0;
        this.isActive = true;
    }

    // Constructor with required fields
    public FlashCard(String question, String answer, String category, String difficulty) {
        this();
        this.question = question;
        this.answer = answer;
        this.category = category;
        this.difficulty = difficulty;
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

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Date getLastReviewed() {
        return lastReviewed;
    }

    public void setLastReviewed(Date lastReviewed) {
        this.lastReviewed = lastReviewed;
    }

    public int getReviewCount() {
        return reviewCount;
    }

    public void setReviewCount(int reviewCount) {
        this.reviewCount = reviewCount;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    // Utility method to increment review count and update last reviewed date
    public void markAsReviewed() {
        this.reviewCount++;
        this.lastReviewed = new Date();
    }
}