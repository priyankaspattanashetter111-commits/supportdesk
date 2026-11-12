package dev.portfolio;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.*;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.time.Instant;
interface Tickets extends JpaRepository<Ticket,Long> {
 Page<Ticket> findByOwner(String owner,Pageable page);
 Page<Ticket> findByStatus(Ticket.Status status,Pageable page);
 Page<Ticket> findByOwnerAndStatus(String owner,Ticket.Status status,Pageable page);
 long countByStatus(Ticket.Status status);
 @Query("select count(t) from Ticket t where t.dueAt < :now and t.status in (dev.portfolio.Ticket$Status.OPEN,dev.portfolio.Ticket$Status.IN_PROGRESS)") long overdue(@Param("now") Instant now);
 @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select t from Ticket t where t.id=:id") Optional<Ticket> locked(@Param("id") Long id);
}
interface TicketEvents extends JpaRepository<TicketEvent,Long>{Page<TicketEvent> findByTicketId(Long id,Pageable page);}
