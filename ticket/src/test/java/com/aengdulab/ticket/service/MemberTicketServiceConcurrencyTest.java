package com.aengdulab.ticket.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.aengdulab.ticket.domain.Member;
import com.aengdulab.ticket.domain.MemberTicket;
import com.aengdulab.ticket.domain.Ticket;
import com.aengdulab.ticket.repository.MemberRepository;
import com.aengdulab.ticket.repository.MemberTicketRepository;
import com.aengdulab.ticket.repository.TicketRepository;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@SuppressWarnings("NonAsciiCharacters")
class MemberTicketServiceConcurrencyTest {

    @Autowired
    private MemberTicketService memberTicketService;

    @Autowired
    private MemberTicketRepository memberTicketRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @BeforeEach
    void setUp() {
        memberTicketRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
        ticketRepository.deleteAllInBatch();
    }

    @Test
    void 동시에_여러_멤버_티켓을_발행할_때_티켓_재고가_올바르게_수정된다() throws InterruptedException {
        Ticket ticket = ticketRepository.save(new Ticket("목성행", 10L));
        int memberCount = 5;
        int threadCount = memberCount * MemberTicket.MEMBER_TICKET_COUNT_MAX;
        List<Member> members = IntStream.range(0, memberCount)
                .mapToObj(memberOrder -> memberRepository.save(new Member("test" + memberOrder)))
                .toList();

        CountDownLatch latch = new CountDownLatch(threadCount);
        try (ExecutorService executorService = Executors.newFixedThreadPool(threadCount)) {
            for (Member member : members) {
                IntStream.range(0, MemberTicket.MEMBER_TICKET_COUNT_MAX)
                        .forEach(ticketCount ->
                                executorService.submit(() -> {
                                    memberTicketService.issue(member.getId(), ticket.getId());
                                    latch.countDown();
                                })
                        );
            }
        }

        latch.await();

        for (Member member : members) {
            long issuedTicketCount = memberTicketRepository.countByMember(member);
            assertThat(issuedTicketCount).isEqualTo(MemberTicket.MEMBER_TICKET_COUNT_MAX);
        }

        assertThat(getTicketQuantity(ticket)).isEqualTo(0);
        assertThat(ticket.getQuantity()).isEqualTo(0);
    }

    private Long getTicketQuantity(Ticket ticket) {
        return ticketRepository.findById(ticket.getId()).orElseThrow().getQuantity();
    }
}
