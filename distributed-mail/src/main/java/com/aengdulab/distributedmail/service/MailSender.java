package com.aengdulab.distributedmail.service;

import com.aengdulab.distributedmail.domain.Question;
import com.aengdulab.distributedmail.domain.Subscribe;

public interface MailSender {

    void send(Subscribe subscribe, Question question);
}
