package dev.portfolio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.time.*;
@Service
class TicketService {
 final Tickets tickets;final TicketEvents events;
 TicketService(Tickets t,TicketEvents e){tickets=t;events=e;}
 static boolean staff(Authentication a){return a.getAuthorities().stream().anyMatch(x->x.getAuthority().equals("ROLE_AGENT"));}
 static ResponseStatusException fail(HttpStatus status,String reason){return new ResponseStatusException(status,reason);}
 static void requireStaff(Authentication a){if(!staff(a))throw fail(HttpStatus.FORBIDDEN,"Agent role required");}
 Ticket visible(long id,Authentication a){Ticket t=tickets.findById(id).orElseThrow(()->fail(HttpStatus.NOT_FOUND,"Ticket not found"));checkVisible(t,a);return t;}
 void checkVisible(Ticket t,Authentication a){if(!staff(a)&&!t.owner.equals(a.getName()))throw fail(HttpStatus.NOT_FOUND,"Ticket not found");}
 @Transactional Ticket create(String title,String description,Ticket.Priority priority,String owner){
  Ticket t=new Ticket();t.title=title;t.description=description;t.priority=priority;t.owner=owner;t.status=Ticket.Status.OPEN;t.createdAt=Instant.now();
  int hours=switch(priority){case LOW->72;case MEDIUM->24;case HIGH->8;case URGENT->2;};t.dueAt=t.createdAt.plus(Duration.ofHours(hours));
  tickets.saveAndFlush(t);events.save(new TicketEvent(t.id,owner,"CREATED",priority.name()));return t;
 }
 @Transactional Ticket claim(long id,Authentication a){
  requireStaff(a);Ticket t=tickets.locked(id).orElseThrow(()->fail(HttpStatus.NOT_FOUND,"Ticket not found"));
  if(t.status==Ticket.Status.CLOSED||t.status==Ticket.Status.RESOLVED)throw fail(HttpStatus.CONFLICT,"Ticket is no longer active");
  if(t.assignee!=null&&!t.assignee.equals(a.getName()))throw fail(HttpStatus.CONFLICT,"Ticket already claimed");
  if(t.assignee==null){t.assignee=a.getName();events.save(new TicketEvent(id,a.getName(),"ASSIGNED",a.getName()));}return t;
 }
 @Transactional Ticket transition(long id,Ticket.Status next,Authentication a){
  Ticket t=tickets.locked(id).orElseThrow(()->fail(HttpStatus.NOT_FOUND,"Ticket not found"));checkVisible(t,a);
  if(next==Ticket.Status.CLOSED){if(!t.owner.equals(a.getName()))throw fail(HttpStatus.FORBIDDEN,"Only the requester can close a resolved ticket");}
  else {requireStaff(a);if(!a.getName().equals(t.assignee))throw fail(HttpStatus.FORBIDDEN,"Claim the ticket before changing its status");}
  boolean valid=switch(t.status){case OPEN->next==Ticket.Status.IN_PROGRESS;case IN_PROGRESS->next==Ticket.Status.RESOLVED;case RESOLVED->next==Ticket.Status.CLOSED;case CLOSED->false;};
  if(!valid)throw fail(HttpStatus.CONFLICT,"Allowed workflow: OPEN to IN_PROGRESS to RESOLVED to CLOSED");
  String previous=t.status.name();t.status=next;if(next==Ticket.Status.RESOLVED)t.resolvedAt=Instant.now();
  events.save(new TicketEvent(id,a.getName(),"STATUS",previous+" -> "+next));return t;
 }
 @Transactional void comment(long id,String text,Authentication a){Ticket t=tickets.locked(id).orElseThrow(()->fail(HttpStatus.NOT_FOUND,"Ticket not found"));checkVisible(t,a);if(t.status==Ticket.Status.CLOSED)throw fail(HttpStatus.CONFLICT,"Closed tickets cannot receive comments");events.save(new TicketEvent(id,a.getName(),"COMMENT",text));}
}
