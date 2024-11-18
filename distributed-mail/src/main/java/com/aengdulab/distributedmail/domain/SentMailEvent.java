package com.aengdulab.distributedmail.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id")
@Getter
@Entity
public class SentMailEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Subscribe subscribe;

    @ManyToOne(fetch = FetchType.LAZY)
    private Question question;

    private boolean isSucceeded;

    public static SentMailEvent success(Subscribe subscribe, Question question) {
        return new SentMailEvent(null, subscribe, question, true);
    }

    public static SentMailEvent fail(Subscribe subscribe, Question question) {
        return new SentMailEvent(null, subscribe, question, false);
    }
}
