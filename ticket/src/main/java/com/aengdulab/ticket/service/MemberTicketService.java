package com.aengdulab.ticket.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberTicketService {

    private final MemberTicketIssueService memberTicketIssueService;

    public synchronized void issue(Long memberId, Long ticketId) {
        memberTicketIssueService.issue(memberId, ticketId);
    }
}
