package com.aengdulab.trenditem.repository;

import com.aengdulab.trenditem.domain.Comment;
import com.aengdulab.trenditem.domain.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    Long countByItem(Item item);
}
