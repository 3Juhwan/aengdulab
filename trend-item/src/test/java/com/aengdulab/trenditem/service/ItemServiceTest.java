package com.aengdulab.trenditem.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.aengdulab.trenditem.config.TestClockConfig;
import com.aengdulab.trenditem.domain.Item;
import com.aengdulab.trenditem.repository.CommentRepository;
import com.aengdulab.trenditem.repository.ItemRepository;
import com.aengdulab.trenditem.repository.LikeRepository;
import com.aengdulab.trenditem.supports.Fixture;
import com.aengdulab.trenditem.supports.ItemReactions;
import com.aengdulab.trenditem.supports.TimeMeasure;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@SpringBootTest
@Import(TestClockConfig.class)
@ExtendWith(SpringExtension.class)
@SuppressWarnings("NonAsciiCharacters")
public class ItemServiceTest {

    @Autowired
    private ItemService itemService;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private LikeRepository likeRepository;

    @BeforeEach
    void setup() {
        commentRepository.deleteAll();
        likeRepository.deleteAll();
        itemRepository.deleteAll();
    }

    @Test
    public void 인기_상품_목록_조회() {
        ItemReactions itemReactions = Fixture.createRandomItems(20, 300, 200);
        itemRepository.saveAll(itemReactions.getItems());
        commentRepository.saveAll(itemReactions.getComments());
        likeRepository.saveAll(itemReactions.getLikes());

        // TODO: 인기 상품 목록 조회 수행 시간을 측정하고 서비스를 개선한다.
        List<Item> trendItems = TimeMeasure.measureTime(() -> itemService.getTrendItems());

        assertThat(trendItems)
                .hasSize(20)
                .extracting(Item::getTitle)
                .containsOnly("trend");
    }
}
