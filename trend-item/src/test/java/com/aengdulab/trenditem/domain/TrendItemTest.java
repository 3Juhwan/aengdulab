package com.aengdulab.trenditem.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.aengdulab.trenditem.supports.Fixture.MINUS_THREE_HOURS;
import static com.aengdulab.trenditem.supports.Fixture.MINUS_TWO_HOURS;
import static com.aengdulab.trenditem.supports.Fixture.NOW;
import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("NonAsciiCharacters")
public class TrendItemTest {

    private Item item1;
    private Item item2;

    @BeforeEach
    public void setup() {
        item1 = new Item("Item 1", "Content 1", 100L, MINUS_THREE_HOURS, null);
        item2 = new Item("Item 2", "Content 2", 150L, MINUS_TWO_HOURS, null);
    }

    @Test
    public void 인기_점수를_올바르게_계산한다() {
        TrendItem trendItem1 = new TrendItem(item1, 10L, 15L, NOW);
        TrendItem trendItem2 = new TrendItem(item2, 5L, 20L, NOW);

        double popularity1 = trendItem1.calculatePopularity();
        double popularity2 = trendItem2.calculatePopularity();

        assertThat(popularity1).isEqualTo(10 * 4 + 15 * 5 + 100L * 3 + 9 * 0.5 + (3 * 60 * -1));
        assertThat(popularity2).isEqualTo(5 * 4 + 20 * 5 + 150L * 3 + 9 * 0.5 + (2 * 60 * -1));
    }

    @Test
    public void 인기_점수_기준으로_비교한다() {
        TrendItem trendItem1 = new TrendItem(item1, 10L, 15L, NOW);
        TrendItem trendItem2 = new TrendItem(item2, 5L, 20L, NOW);
        TrendItem trendItem3 = new TrendItem(item1, 10L, 15L, NOW);

        assertThat(trendItem1).isGreaterThan(trendItem2);
        assertThat(trendItem1).isEqualByComparingTo(trendItem3);
        assertThat(trendItem2).isLessThan(trendItem3);
    }
}
