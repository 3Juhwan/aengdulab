package com.aengdulab.ticket.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@EqualsAndHashCode(of = "id")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private Integer issuedTicketCount;

    public Member(String name) {
        this(null, name, 0);
    }

    public Member(String name, Integer issuedTicketCount) {
        this(null, name, issuedTicketCount);
    }

    public Member(Long id, String name, Integer issuedTicketCount) {
        this.id = id;
        this.name = name;
        this.issuedTicketCount = issuedTicketCount;
    }

    public void increaseIssuedTicketCount() {
        issuedTicketCount++;
    }

    public boolean canIssueTicket() {
        return issuedTicketCount < MemberTicket.MEMBER_TICKET_COUNT_MAX;
    }
}
