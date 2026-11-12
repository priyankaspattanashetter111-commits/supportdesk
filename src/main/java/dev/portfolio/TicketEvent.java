package dev.portfolio;
import jakarta.persistence.*;
import java.time.Instant;
@Entity @Table(name="ticket_events")
class TicketEvent {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long id;
 Long ticketId;String actor;String action;
 @Column(length=2000) String detail;
 Instant happenedAt;
 protected TicketEvent(){}
 TicketEvent(Long id,String actor,String action,String detail){this.ticketId=id;this.actor=actor;this.action=action;this.detail=detail;this.happenedAt=Instant.now();}
}
