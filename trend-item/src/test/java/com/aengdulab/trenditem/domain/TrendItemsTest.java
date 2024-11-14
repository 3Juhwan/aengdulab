package com.aengdulab.trenditem.domain;

import java.util.ArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.aengdulab.trenditem.supports.Fixture.*;
import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("NonAsciiCharacters")
public class TrendItemsTest {

    private Item item1;
    private Item item2;
    private Item item3;
    private Item item4;

    @BeforeEach
    public void setup() {
        item1 = new Item("Item 1", "Content 1", 100L, MINUS_THREE_HOURS, null);
        item2 = new Item("Item 2", "Content 2", 150L, MINUS_TWO_HOURS, null);
        item3 = new Item("Item 3", "Content 3", 120L, MINUS_FOUR_HOURS, null);
        item4 = new Item("Item 4", "Content 4", 90L, MINUS_ONE_HOUR, null);
    }

    @Test
    public void 인기_점수_기준으로_정렬한다() {
        TrendItem trendItem1 = new TrendItem(item1, 10L, 15L, NOW);
        TrendItem trendItem2 = new TrendItem(item2, 5L, 20L, NOW);
        TrendItem trendItem3 = new TrendItem(item3, 10L, 15L, NOW);
        TrendItem trendItem4 = new TrendItem(item4, 7L, 10L, NOW);

        List<TrendItem> trendItems = List.of(trendItem1, trendItem2, trendItem3, trendItem4);
        TrendItems trendItemsContainer = new TrendItems(trendItems);

        List<Item> items = trendItemsContainer.getItems();

        assertThat(items).hasSize(4);
        assertThat(items.get(0).getTitle()).isEqualTo("Item 2");
        assertThat(items.get(1).getTitle()).isEqualTo("Item 4");
        assertThat(items.get(2).getTitle()).isEqualTo("Item 1");
        assertThat(items.get(3).getTitle()).isEqualTo("Item 3");
    }

    @Test
    public void 인기_상품_개수를_제한한다() {
        TrendItem trendItem1 = new TrendItem(item1, 10L, 15L, NOW);
        List<TrendItem> trendItems = new ArrayList<>();
        for (int i = 0; i < 21; i++) {
            trendItems.add(trendItem1);
        }
        TrendItems trendItemsContainer = new TrendItems(trendItems);

        List<Item> items = trendItemsContainer.getItems();

        assertThat(items).hasSize(20);
    }
}
