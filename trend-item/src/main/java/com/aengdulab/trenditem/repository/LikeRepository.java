package com.aengdulab.trenditem.repository;

import com.aengdulab.trenditem.domain.Item;
import com.aengdulab.trenditem.domain.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {

    Long countByItem(Item item);
}
