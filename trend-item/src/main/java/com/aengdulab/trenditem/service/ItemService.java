package com.aengdulab.trenditem.service;

import com.aengdulab.trenditem.domain.Item;
import com.aengdulab.trenditem.domain.TrendItem;
import com.aengdulab.trenditem.domain.TrendItems;
import com.aengdulab.trenditem.repository.CommentRepository;
import com.aengdulab.trenditem.repository.ItemRepository;
import com.aengdulab.trenditem.repository.LikeRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemService {

    private static final int TREND_ITEM_HOURS = 5;
    
    private final ItemRepository itemRepository;
    private final CommentRepository commentRepository;
    private final LikeRepository likeRepository;
    private final Clock clock;

    public List<Item> getTrendItems() {
        LocalDateTime recentItemCutoff = LocalDateTime.now(clock).minusHours(TREND_ITEM_HOURS);
        List<Item> items = itemRepository.findAllByPostedAtAfterAndSoldOutAtIsNull(recentItemCutoff);
        TrendItems trendItems = createTrendItems(items);

        return trendItems.getItems();
    }

    private TrendItems createTrendItems(List<Item> items) {
        List<TrendItem> trendItems = items.stream()
                .map(this::createTrendItem)
                .toList();

        return new TrendItems(trendItems);
    }

    private TrendItem createTrendItem(Item item) {
        Long comments = commentRepository.countByItem(item);
        Long likes = likeRepository.countByItem(item);

        return new TrendItem(item, comments, likes, LocalDateTime.now(clock));
    }
}
