package com.aengdulab.distributedmail.service;

import com.aengdulab.distributedmail.ServerPortProvider;
import com.aengdulab.distributedmail.domain.Question;
import com.aengdulab.distributedmail.domain.Subscribe;
import com.aengdulab.distributedmail.domain.SubscribeQuestionMessage;
import com.aengdulab.distributedmail.repository.QuestionRepository;
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
    private final QuestionRepository questionRepository;
    private final ServerPortProvider serPortProvider;

    @Transactional
    @Scheduled(cron = "0 0 9 * * *", zone = "Asia/Seoul")
    public void sendQuestion() {
        List<Subscribe> subscribes = getSubscribesForThisServer(serPortProvider.getServerPort());
        sendQuestionMails(subscribes);
        updateNextQuestions(subscribes);
    }

    private List<Subscribe> getSubscribesForThisServer(int serPort) {
        int serverIndex = getServerIndex(serPort);
        int serverCount = getTotalServerCount();
        return subscribeRepository.findAll().stream()
                .filter(subscribe -> isResponsibleServer(subscribe.getId().intValue(), serverCount, serverIndex))
                .toList();
    }

    private int getServerIndex(int serPort) {
        if (serPort == 8080) {
            return 0;
        } else if (serPort == 9090) {
            return 1;
        }
        return 2;
    }

    private int getTotalServerCount() {
        return 3;
    }

    private boolean isResponsibleServer(int subscribeId, int serverCount, int serverIndex) {
        return (subscribeId % serverCount) == serverIndex;
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
