package com.aengdulab.distributedmail.repository;

import com.aengdulab.distributedmail.domain.Question;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    @Query(value = "select * from question where id > :id order by id asc limit 1", nativeQuery = true)
    Optional<Question> findNextQuestion(@Param("id") Long id);
}
