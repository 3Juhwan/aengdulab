package com.aengdulab.trenditem.supports;

import com.aengdulab.trenditem.domain.Comment;
import com.aengdulab.trenditem.domain.Item;
import com.aengdulab.trenditem.domain.Like;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Fixture {
    public static LocalDateTime NOW = LocalDateTime.of(2024, 11, 14, 12, 0, 0);

    public static LocalDateTime MINUS_ONE_HOUR = NOW.minusHours(1);
    public static LocalDateTime MINUS_TWO_HOURS = NOW.minusHours(2);
    public static LocalDateTime MINUS_THREE_HOURS = NOW.minusHours(3);
    public static LocalDateTime MINUS_FOUR_HOURS = NOW.minusHours(4);
    public static LocalDateTime MINUS_SIX_HOURS = NOW.minusHours(6);

    public static Instant NOW_INSTANT = NOW.atZone(ZoneId.systemDefault()).toInstant();
    public static Clock FIXED_CLOCK = Clock.fixed(NOW_INSTANT, ZoneId.systemDefault());

    private static final Random RANDOM = new Random();

    /**
     * 여러 종류의 상품과 그 상품에 대한 댓글과 좋아요를 생성하여 `ItemReactions` 객체로 결합하는 메서드입니다.
     *
     * <p>상품의 종류는 다음과 같습니다:</p>
     * <ul>
     *     <li><b>TrendItem</b>: 인기 점수가 높고 인기 상품 조건을 충족하는 상품. (다섯 시간 내에 게시된, 많은 좋아요와 댓글을 가진 판매 진행 상품)</li>
     *     <li><b>OutOfTrendItem</b>: 인기 상품 조건을 충족하지만 TrendItem보다 인기 점수가 낮은 상품.</li>
     *     <li><b>NonEligibleItem</b>: 인기 상품의 조건을 충족하지 않는 상품. (게시된 지 오래되었거나 판매가 완료된 상품.) </li>
     * </ul>
     *
     * @param outOfTrendItemCount 생성할 `OutOfTrendItem`의 수
     * @param nonEligibleItemCount 생성할 `NonEligibleItem`의 수
     * @return `ItemReactions` 상품 목록과 그에 대한 댓글과 좋아요 목록을 포함
     */
    public static ItemReactions createRandomItems(int trendItemCount, int outOfTrendItemCount, int nonEligibleItemCount) {
        ItemReactions trendItemsReactions = createTrendItemReactions(trendItemCount);
        ItemReactions outOfTrendItemsReactions = createOutOfTrendItemReactions(outOfTrendItemCount);
        ItemReactions nonEligibleItemReactions = createNonEligibleItemReactions(nonEligibleItemCount);
        return ItemReactions.concat(trendItemsReactions, outOfTrendItemsReactions, nonEligibleItemReactions);
    }

    public static ItemReactions createTrendItemReactions(int count) {
        List<Item> items = new ArrayList<>();
        List<Comment> comments = new ArrayList<>();
        List<Like> likes = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Item item = new Item("trend", getRandomContent(100, 200), getRandomViews(1000, 5000), MINUS_ONE_HOUR);
            comments.addAll(createRandomComments(item, 10, 25));
            likes.addAll(createRandomLikes(item, 60, 300));
            items.add(item);
        }
        return new ItemReactions(items, comments, likes);
    }

    public static ItemReactions createOutOfTrendItemReactions(int count) {
        List<Item> items = new ArrayList<>();
        List<Comment> comments = new ArrayList<>();
        List<Like> likes = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            LocalDateTime postedAt = NOW.minusHours(RANDOM.nextLong(2, 5));
            Item item = new Item("outOfTrend", getRandomContent(5, 20), getRandomViews(100, 200), postedAt);
            comments.addAll(createRandomComments(item, 0, 5));
            likes.addAll(createRandomLikes(item, 3, 10));
            items.add(item);
        }
        return new ItemReactions(items, comments, likes);
    }

    public static ItemReactions createNonEligibleItemReactions(int count) {
        List<Item> items = new ArrayList<>();
        List<Comment> comments = new ArrayList<>();
        List<Like> likes = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Item item = createNonEligibleItem();
            comments.addAll(createRandomComments(item, 0, 5));
            likes.addAll(createRandomLikes(item, 3, 10));
            items.add(item);
        }
        return new ItemReactions(items, comments, likes);
    }

    private static Item createNonEligibleItem() {
        if (RANDOM.nextBoolean()) {
            return new Item("nonEligible", getRandomContent(100, 200), getRandomViews(100, 20000), MINUS_SIX_HOURS);
        }
        return new Item("nonEligible", getRandomContent(100, 200), getRandomViews(100, 20000), MINUS_TWO_HOURS, NOW);
    }

    private static String getRandomContent(int minLength, int maxLength) {
        return "1".repeat(RANDOM.nextInt(minLength, maxLength));
    }

    private static Long getRandomViews(int minViews, int maxViews) {
        return RANDOM.nextLong(minViews, maxViews);
    }

    private static List<Comment> createRandomComments(Item item, int minCount, int maxCount) {
        List<Comment> comments = new ArrayList<>();
        int count = RANDOM.nextInt(minCount, maxCount);
        for (int i = 0; i < count; i++) {
            comments.add(new Comment(item, 1L, "content"));
        }
        return comments;
    }

    private static List<Like> createRandomLikes(Item item, int minCount, int maxCount) {
        List<Like> likes = new ArrayList<>();
        int count = RANDOM.nextInt(minCount, maxCount);
        for (int i = 0; i < count; i++) {
            likes.add(new Like(item, 1L));
        }
        return likes;
    }
}
