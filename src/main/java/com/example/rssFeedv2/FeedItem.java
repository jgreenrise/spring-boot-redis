package com.example.rssFeedv2;

import java.io.Serializable;
import java.util.Date;

public class FeedItem implements Serializable {
    private String id;
    private String title;
    private String description;
    private String link;
    private Date pubDate;
    private String type; // "RSS" or "FLASHCARD"
    private String category; // For flash cards
    private String difficulty; // For flash cards

    // Default constructor
    public FeedItem() {
    }

    // Constructor for RSS items
    public FeedItem(String id, String title, String description, String link, Date pubDate, String type) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.link = link;
        this.pubDate = pubDate;
        this.type = type;
    }

    // Constructor for flash cards
    public FeedItem(String id, String title, String description, String link, Date pubDate, String type, String category, String difficulty) {
        this(id, title, description, link, pubDate, type);
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public Date getPubDate() {
        return pubDate;
    }

    public void setPubDate(Date pubDate) {
        this.pubDate = pubDate;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    @Override
    public String toString() {
        return "FeedItem{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", link='" + link + '\'' +
                ", pubDate=" + pubDate +
                ", type='" + type + '\'' +
                ", category='" + category + '\'' +
                ", difficulty='" + difficulty + '\'' +
                '}';
    }
}