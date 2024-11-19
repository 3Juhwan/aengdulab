package com.aengdulab.distributedmail.service;

import com.aengdulab.distributedmail.domain.Question;
import com.aengdulab.distributedmail.domain.Subscribe;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class MailSenderImpl implements MailSender {

    private static final String FROM_MAIL_ADDRESS = "test@example.com";

    private final JavaMailSender mailSender;

    @Override
    public void send(Subscribe subscribe, Question question) {
        try {
            SimpleMailMessage mailMessage = makeMailMessage(subscribe, question);
            mailSender.send(mailMessage);
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private SimpleMailMessage makeMailMessage(Subscribe subscribe, Question question) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(subscribe.getEmail());
        mailMessage.setSubject(question.getTitle());
        mailMessage.setText(question.getContent());
        mailMessage.setFrom(FROM_MAIL_ADDRESS);
        return mailMessage;
    }

}
