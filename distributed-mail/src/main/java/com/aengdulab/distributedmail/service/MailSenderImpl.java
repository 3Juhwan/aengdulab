package com.aengdulab.distributedmail.service;

import com.aengdulab.distributedmail.domain.Question;
import com.aengdulab.distributedmail.domain.Subscribe;
import org.springframework.stereotype.Component;

@Component
public class MailSenderImpl implements MailSender {

    @Override
    public void send(Subscribe subscribe, Question question) {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
