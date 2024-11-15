package com.aengdulab.distributedmail.controller;

import com.aengdulab.distributedmail.service.SendQuestionScheduler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
public class SendMailController {

    private final SendQuestionScheduler sendQuestionScheduler;

    @PostMapping("/send-mail")
    public ResponseEntity<Void> sendMail() {
        sendQuestionScheduler.sendQuestion();
        return ResponseEntity.ok().build();
    }
}
