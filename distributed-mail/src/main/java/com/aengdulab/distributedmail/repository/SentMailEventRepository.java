package com.aengdulab.distributedmail.repository;

import com.aengdulab.distributedmail.domain.SentMailEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SentMailEventRepository extends JpaRepository<SentMailEvent, Long> {
}
