package com.aengdulab.trenditem.repository;

import com.aengdulab.trenditem.domain.Item;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    List<Item> findAllByPostedAtAfterAndSoldOutAtIsNull(LocalDateTime postedAt);
}
