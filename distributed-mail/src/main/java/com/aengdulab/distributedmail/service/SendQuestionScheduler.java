package com.aengdulab.distributedmail.service;

import com.aengdulab.distributedmail.domain.Question;
import com.aengdulab.distributedmail.domain.Subscribe;
import com.aengdulab.distributedmail.domain.SubscribeQuestionMessage;
import com.aengdulab.distributedmail.repository.QuestionRepository;
import com.aengdulab.distributedmail.repository.SubscribeRepository;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class SendQuestionScheduler {

    private final QuestionSender questionSender;
    private final SubscribeRepository subscribeRepository;
    private final QuestionRepository questionRepository;

    @Scheduled(cron = "0 0 9 * * *", zone = "Asia/Seoul")
    public void sendQuestion() {
        List<Subscribe> subscribes = subscribeRepository.findAll();

        subscribes.stream()
                .map(this::choiceQuestion)
                .filter(Objects::nonNull)
                .forEach(questionSender::sendQuestion);
    }

    private SubscribeQuestionMessage choiceQuestion(Subscribe subscribe) {
        return getNextQuestion(subscribe)
                .map(question -> new SubscribeQuestionMessage(subscribe, question))
                .orElse(null);
    }

    private Optional<Question> getNextQuestion(Subscribe subscribe) {
        Long questionSequence = subscribe.getNextQuestionSequence();
        return questionRepository.findById(questionSequence)
                .or(() -> {
                    log.error("[질문 조회 실패] subscribeId = {}, nextQuestionSequence = {}",
                            subscribe.getId(), questionSequence);
                    return Optional.empty();
                });
    }
}
