package com.aengdulab.ticket.repository;

import com.aengdulab.ticket.domain.Ticket;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    @Lock(LockModeType.OPTIMISTIC)
    @Query("select t from Ticket t where t.id = :ticketId")
    Optional<Ticket> findByIdWithLock(Long ticketId);
}
