package com.aengdulab.trenditem.domain;

import java.util.List;

public class TrendItems {

    private static final int TREND_ITEMS_LIMIT = 20;

    private final List<TrendItem> trendItems;

    public TrendItems(List<TrendItem> trendItems) {
        this.trendItems = trendItems.stream()
                .sorted()
                .limit(TREND_ITEMS_LIMIT)
                .toList();
    }

    public List<Item> getItems() {
        return trendItems.stream()
                .map(TrendItem::getItem)
                .toList();
    }
}
