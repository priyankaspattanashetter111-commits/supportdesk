package dev.portfolio;
import jakarta.persistence.*;
import java.time.Instant;
@Entity @Table(name="tickets")
class Ticket {
 enum Priority {LOW,MEDIUM,HIGH,URGENT}
 enum Status {OPEN,IN_PROGRESS,RESOLVED,CLOSED}
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long id;
 String title;
 @Column(length=4000) String description;
 String owner;String assignee;
 @Enumerated(EnumType.STRING) Priority priority;
 @Enumerated(EnumType.STRING) Status status;
 Instant createdAt;Instant dueAt;Instant resolvedAt;
 @Version long version;
 protected Ticket(){}
}
