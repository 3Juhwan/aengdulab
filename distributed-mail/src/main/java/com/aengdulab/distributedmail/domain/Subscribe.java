package com.aengdulab.distributedmail.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
public class Subscribe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    @JoinColumn(name = "question_id", nullable = false)
    @ManyToOne(fetch = FetchType.EAGER)
    private Question question;

    public Subscribe(Long id, String email, Question question) {
        this.id = id;
        this.email = email;
        this.question = question;
    }

    public void setNextQuestion(Question question) {
        this.question = question;
    }
}
