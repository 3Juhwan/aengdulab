package com.aengdulab.trenditem.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.Duration;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String content;
    private Long views;
    private LocalDateTime postedAt;
    private LocalDateTime soldOutAt;

    public Item(String title, String content, Long views, LocalDateTime postedAt, LocalDateTime soldOutAt) {
        this(null, title, content, views, postedAt, soldOutAt);
    }

    public Item(String title, String content, Long views, LocalDateTime postedAt) {
        this(null, title, content, views, postedAt, null);
    }

    public Item(Long id, String title, String content, Long views, LocalDateTime postedAt, LocalDateTime soldOutAt) {
        validateTitle(title);
        validateContent(content);
        validateViews(views);
        validatePostedAt(postedAt);
        this.id = id;
        this.title = title;
        this.content = content;
        this.views = views;
        this.postedAt = postedAt;
        this.soldOutAt = soldOutAt;
    }

    private void validateTitle(String title) {
        if (title == null || title.isEmpty()) {
            throw new IllegalArgumentException("Title cannot be null or empty");
        }
    }

    private void validateContent(String content) {
        if (content == null || content.isEmpty()) {
            throw new IllegalArgumentException("Content cannot be null or empty");
        }
    }

    private void validateViews(Long views) {
        if (views == null) {
            throw new IllegalArgumentException("Views cannot be null or empty");
        }
    }

    private void validatePostedAt(LocalDateTime postedAt) {
        if (postedAt == null) {
            throw new IllegalArgumentException("PostedAt cannot be null");
        }
    }

    public Long calculateElapsedMinutes(LocalDateTime comparisonTime) {
        return Duration.between(postedAt, comparisonTime).toMinutes();
    }

    public int getContentLength() {
        return content.length();
    }
}
