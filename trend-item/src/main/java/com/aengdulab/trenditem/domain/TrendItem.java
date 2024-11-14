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
    private static final int ELAPSED_TIME_WEIGHT = 2;

    private final Item item;
    private final Long comments;
    private final Long likes;

    public TrendItem(Item item, Long comments, Long likes) {
        this.item = item;
        this.comments = comments;
        this.likes = likes;
    }

    public double calculatePopularity() {
        long elapsedTime = Duration.between(item.getPostedAt(), LocalDateTime.now()).toMinutes();
        return (comments * COMMENT_WEIGHT) +
                (likes * LIKE_WEIGHT) +
                (item.getViews() * VIEW_WEIGHT) +
                (item.getContentLength() * CONTENT_LENGTH_WEIGHT) -
                (elapsedTime * ELAPSED_TIME_WEIGHT);
    }

    @Override
    public int compareTo(TrendItem o) {
        return Double.compare(calculatePopularity(), o.calculatePopularity());
    }
}
