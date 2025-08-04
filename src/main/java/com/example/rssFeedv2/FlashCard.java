package com.example.rssFeedv2;

import java.io.Serializable;
import java.util.Date;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@RedisHash
public class FlashCard implements Serializable {
    @Id
    private String id;
    private String front;
    private String back;
    private Date createdDate;
    private Date lastReviewed;
    private int reviewCount;

    // Default constructor
    public FlashCard() {
        this.reviewCount = 0;
        this.createdDate = new Date();
    }

    // Constructor with front and back
    public FlashCard(String front, String back) {
        this();
        this.front = front;
        this.back = back;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFront() {
        return front;
    }

    public void setFront(String front) {
        this.front = front;
    }

    public String getBack() {
        return back;
    }

    public void setBack(String back) {
        this.back = back;
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
}