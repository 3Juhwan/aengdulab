package com.aengdulab.distributedmail.service;

import com.aengdulab.distributedmail.domain.Question;
import com.aengdulab.distributedmail.domain.Subscribe;
import com.aengdulab.distributedmail.repository.QuestionRepository;
import com.aengdulab.distributedmail.repository.SubscribeRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class SubscribeQuestionSequenceScheduler {

    private final SubscribeRepository subscribeRepository;
    private final QuestionRepository questionRepository;

    @Transactional
    @Scheduled(cron = "0 0 22 * * *", zone = "Asia/Seoul")
    public void updateQuestionSequence() {
        List<Subscribe> subscribes = subscribeRepository.findAll();
        updateNextQuestions(subscribes);
    }

    private void updateNextQuestions(List<Subscribe> subscribes) {
        subscribes.forEach(
                subscribe -> {
                    Question nextQuestion = getNextQuestion(subscribe.getQuestion());
                    subscribe.setNextQuestion(nextQuestion);
                }
        );
    }

    private Question getNextQuestion(Question question) {
        return questionRepository.findNextQuestion(question.getId())
                .orElseThrow(() -> new IllegalArgumentException("다음 질문이 없습니다. 현재 질문 id=" + question.getId()));
    }
}
