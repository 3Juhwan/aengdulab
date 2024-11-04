package com.aengdulab.ticket.repository;

import com.aengdulab.ticket.domain.Member;
import com.aengdulab.ticket.domain.MemberTicket;
import jakarta.persistence.LockModeType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberTicketRepository extends JpaRepository<MemberTicket, Long> {

    int countByMember(Member member);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select mt from MemberTicket mt where mt.member = :member")
    List<MemberTicket> findAllByMemberWithLock(Member member);
}
