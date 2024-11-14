package com.aengdulab.trenditem.domain;

import java.time.Duration;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class TrendItem implements Comparable<TrendItem> {

    private static final int COMMENT_WEIGHT = 4;
    private static final int LIKE_WEIGHT = 5;
    private static final int VIEW_WEIGHT = 3;
    private static final double CONTENT_LENGTH_WEIGHT = 0.5;
    private static final double ELAPSED_TIME_WEIGHT = -1;

    private final Item item;
    private final Long comments;
    private final Long likes;
    private final LocalDateTime now;

    public TrendItem(Item item, Long comments, Long likes, LocalDateTime now) {
        this.item = item;
        this.comments = comments;
        this.likes = likes;
        this.now = now;
    }

    public double calculatePopularity() {
        return (comments * COMMENT_WEIGHT) +
                (likes * LIKE_WEIGHT) +
                (item.getViews() * VIEW_WEIGHT) +
                (item.getContentLength() * CONTENT_LENGTH_WEIGHT) +
                (item.calculateElapsedMinutes(now) * ELAPSED_TIME_WEIGHT);
    }

    @Override
    public int compareTo(TrendItem o) {
        return Double.compare(o.calculatePopularity(), calculatePopularity());
    }
}
