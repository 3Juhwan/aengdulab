package com.aengdulab.distributedmail.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class SubscribeQuestionMessage {

    private final Subscribe subscribe;
    private final Question question;
}
