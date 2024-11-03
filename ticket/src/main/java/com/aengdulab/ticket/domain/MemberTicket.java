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
public class MemberTicket {

    public static final int MEMBER_TICKET_COUNT_MAX = 2;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long memberId;

    private Long ticketId;

    public MemberTicket(Long memberId, Long ticketId) {
        this(null, memberId, ticketId);
    }

    public MemberTicket(Long id, Long memberId, Long ticketId) {
        this.id = id;
        this.memberId = memberId;
        this.ticketId = ticketId;
    }
}
