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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@SuppressWarnings("NonAsciiCharacters")
class MemberTicketServiceConcurrencyTest {

    private Logger log = LoggerFactory.getLogger(MemberTicketServiceConcurrencyTest.class);

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
        long startTime = System.currentTimeMillis();
        try (ExecutorService executorService = Executors.newFixedThreadPool(threadCount)) {
            for (Member member : members) {
                IntStream.range(0, MemberTicket.MEMBER_TICKET_COUNT_MAX)
                        .forEach(ticketCount ->
                                executorService.submit(() -> {
                                    try {
                                        memberTicketService.issue(member.getId(), ticket.getId());
                                    } catch (Exception e) {
                                    } finally {
                                        latch.countDown();
                                    }
                                })
                        );
            }
        }

        latch.await();
        long endTime = System.currentTimeMillis();
        log.info("[멤버 티켓 최댓값에 맞게 계정별로 발행이 제한된다] 수행 시간 : {}ms", (endTime - startTime));

        for (Member member : members) {
            long issuedTicketCount = memberTicketRepository.countByMember(member);
            assertThat(issuedTicketCount).isEqualTo(MemberTicket.MEMBER_TICKET_COUNT_MAX);
        }

        assertThat(getTicketQuantity(ticket)).isEqualTo(0);
    }

    @Test
    void 동시에_여러_멤버_티켓을_발행할_때_티켓_재고가_올바르게_수정된다_큰_수() throws InterruptedException {
        Ticket ticket = ticketRepository.save(new Ticket("목성행", 10L));
        int memberCount = 20;
        int threadCount = memberCount * MemberTicket.MEMBER_TICKET_COUNT_MAX;
        List<Member> members = IntStream.range(0, memberCount)
                .mapToObj(memberOrder -> memberRepository.save(new Member("test" + memberOrder)))
                .toList();

        CountDownLatch latch = new CountDownLatch(threadCount);
        long startTime = System.currentTimeMillis();
        try (ExecutorService executorService = Executors.newFixedThreadPool(threadCount)) {
            for (Member member : members) {
                IntStream.range(0, MemberTicket.MEMBER_TICKET_COUNT_MAX)
                        .forEach(ticketCount ->
                                executorService.submit(() -> {
                                    try {
                                        memberTicketService.issue(member.getId(), ticket.getId());
                                    } catch (Exception e) {
                                    } finally {
                                        latch.countDown();
                                    }
                                })
                        );
            }
        }

        latch.await();
        long endTime = System.currentTimeMillis();
        log.info("[멤버 티켓 최댓값에 맞게 계정별로 발행이 제한된다] 수행 시간 : {}ms", (endTime - startTime));

        for (Member member : members) {
            long issuedTicketCount = memberTicketRepository.countByMember(member);
            assertThat(issuedTicketCount).isLessThanOrEqualTo(MemberTicket.MEMBER_TICKET_COUNT_MAX);
        }

        assertThat(getTicketQuantity(ticket)).isEqualTo(0);
    }

    @Test
    void 동시에_특정_멤버_티켓을_발행할_때_멤버_티켓_발행이_제한된다() throws InterruptedException {
        Ticket jupiterTicket = ticketRepository.save(new Ticket("목성행", 100L));
        Ticket marsTicket = ticketRepository.save(new Ticket("화성행", 100L));
        int memberCount = 3;
        int ticketIssueCount = 3 * MemberTicket.MEMBER_TICKET_COUNT_MAX;
        int threadCount = memberCount * ticketIssueCount;
        List<Member> members = IntStream.range(0, memberCount)
                .mapToObj(memberOrder -> memberRepository.save(new Member("멤버" + memberOrder)))
                .toList();

        CountDownLatch latch = new CountDownLatch(threadCount);
        long startTime = System.currentTimeMillis();
        try (ExecutorService executorService = Executors.newFixedThreadPool(threadCount)) {
            for (Member member : members) {
                IntStream.range(0, ticketIssueCount)
                        .forEach(ticketCount -> {
                            Long ticketId = getRandomTicket(jupiterTicket, marsTicket).getId();
                            executorService.submit(() -> {
                                try {
                                    memberTicketService.issue(member.getId(), ticketId);
                                } catch (IllegalArgumentException e) {
                                    System.out.println(e.getMessage());
                                } finally {
                                    latch.countDown();
                                }
                            });
                        });
            }
        }

        latch.await();
        long endTime = System.currentTimeMillis();
        log.info("[멤버 티켓 최댓값에 맞게 계정별로 발행이 제한된다] 수행 시간 : {}ms", (endTime - startTime));

        for (Member member : members) {
            long issuedTicketCount = memberTicketRepository.countByMember(member);
            assertThat(issuedTicketCount).isEqualTo(MemberTicket.MEMBER_TICKET_COUNT_MAX);
        }
    }

    private Ticket getRandomTicket(Ticket... tickets) {
        int ticketOrder = (int) (Math.random() * tickets.length);
        return tickets[ticketOrder];
    }

    private Long getTicketQuantity(Ticket ticket) {
        return ticketRepository.findById(ticket.getId()).orElseThrow().getQuantity();
    }
}
