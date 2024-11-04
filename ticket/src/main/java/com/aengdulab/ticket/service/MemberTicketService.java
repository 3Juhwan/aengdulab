package com.aengdulab.ticket.service;

import com.aengdulab.ticket.domain.Member;
import com.aengdulab.ticket.domain.MemberTicket;
import com.aengdulab.ticket.domain.Ticket;
import com.aengdulab.ticket.repository.MemberRepository;
import com.aengdulab.ticket.repository.MemberTicketRepository;
import com.aengdulab.ticket.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberTicketService {

    private final MemberRepository memberRepository;
    private final TicketRepository ticketRepository;
    private final MemberTicketRepository memberTicketRepository;

    @Transactional
    public synchronized void issue(Long memberId, Long ticketId) {
        Member member = getMember(memberId);
        Ticket ticket = getTicket(ticketId);
        MemberTicket memberTicket = new MemberTicket(member, ticket);
        if (!memberTicket.issuable()) {
            throw new IllegalArgumentException("티켓을 발급할 수 없습니다.");
        }
        memberTicketRepository.save(memberTicket);
        ticket.decrementQuantity();
        member.increaseIssuedTicketCount();
    }

    private Member getMember(Long memberId) {
        return memberRepository.findByIdWithLock(memberId)
                .orElseThrow(() -> new IllegalArgumentException("멤버가 존재하지 않습니다."));
    }

    private Ticket getTicket(Long ticketId) {
        return ticketRepository.findByIdWithLock(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("티켓이 존재하지 않습니다."));
    }
}
