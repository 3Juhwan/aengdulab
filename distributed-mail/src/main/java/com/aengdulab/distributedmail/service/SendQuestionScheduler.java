package com.aengdulab.distributedmail.service;

import com.aengdulab.distributedmail.domain.Subscribe;
import com.aengdulab.distributedmail.domain.SubscribeQuestionMessage;
import com.aengdulab.distributedmail.repository.SubscribeRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Component
public class SendQuestionScheduler {

    private final QuestionSender questionSender;
    private final SubscribeRepository subscribeRepository;

    @Transactional
    @Scheduled(cron = "0 0 9 * * *", zone = "Asia/Seoul")
    public void sendQuestion() {
        List<Subscribe> subscribes = subscribeRepository.findAll();
        sendQuestionMails(subscribes);
    }

    private void sendQuestionMails(List<Subscribe> subscribes) {
        subscribes.stream()
                .flatMap(subscribe -> choiceQuestion(subscribe).stream())
                .forEach(questionSender::sendQuestion);
    }

    private Optional<SubscribeQuestionMessage> choiceQuestion(Subscribe subscribe) {
        return Optional.ofNullable(subscribe.getQuestion())
                .map(nextQuestion -> new SubscribeQuestionMessage(subscribe, nextQuestion))
                .or(() -> {
                    log.error("[질문 조회 실패] subscribeId = {}", subscribe.getId());
                    return Optional.empty();
                });
    }
}
