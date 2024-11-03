package com.aengdulab.ticket.repository;

import com.aengdulab.ticket.domain.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    @Modifying
    @Query("update Ticket t set t.quantity = t.quantity - 1 where t.id = :ticketId")
    void decrementQuantity(Long ticketId);
}
