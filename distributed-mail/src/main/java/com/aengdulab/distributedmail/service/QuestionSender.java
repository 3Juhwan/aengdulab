package com.aengdulab.distributedmail.service;

import com.aengdulab.distributedmail.domain.Question;
import com.aengdulab.distributedmail.domain.SentMailEvent;
import com.aengdulab.distributedmail.domain.Subscribe;
import com.aengdulab.distributedmail.domain.SubscribeQuestionMessage;
import com.aengdulab.distributedmail.repository.SentMailEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class QuestionSender {

    private final MailSender mailSender;
    private final SentMailEventRepository sentMailEventRepository;

    @Async
    public void sendQuestion(SubscribeQuestionMessage subscribeQuestionMessage) {
        Question question = subscribeQuestionMessage.getQuestion();
        Subscribe subscribe = subscribeQuestionMessage.getSubscribe();

        try {
            mailSender.send(subscribe, question);
            log.info("[메일 발송 성공] email = {}, questionId = {}", subscribe.getEmail(), question.getId());
            sentMailEventRepository.save(SentMailEvent.success(subscribe, question));
        } catch (Exception e) {
            log.error("[메일 발송 실패] email = {}, questionId = {}", subscribe.getEmail(), question.getId(), e);
            sentMailEventRepository.save(SentMailEvent.fail(subscribe, question));
        }
    }
}
