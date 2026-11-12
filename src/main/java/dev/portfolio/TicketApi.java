package dev.portfolio;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.data.domain.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.List;
@RestController @RequestMapping("/api")
class TicketApi {
 final Tickets tickets;final TicketEvents events;final TicketService service;
 TicketApi(Tickets t,TicketEvents e,TicketService s){tickets=t;events=e;service=s;}
 record Create(@NotBlank @Size(max=160) String title,@NotBlank @Size(max=4000) String description,@NotNull Ticket.Priority priority){}
 record Change(@NotNull Ticket.Status status){}
 record Comment(@NotBlank @Size(max=2000) String text){}
 record View(Long id,String title,String description,String owner,String assignee,Ticket.Priority priority,Ticket.Status status,Instant dueAt,Instant resolvedAt){static View of(Ticket t){return new View(t.id,t.title,t.description,t.owner,t.assignee,t.priority,t.status,t.dueAt,t.resolvedAt);}}
 record EventView(Long id,String actor,String action,String detail,Instant happenedAt){}
 record PageView<T>(List<T> content,int page,int size,long totalElements){}
 static Pageable page(int n,int size){if(n<0||size<1||size>100)throw TicketService.fail(HttpStatus.BAD_REQUEST,"Page must be nonnegative and size 1 to 100");return PageRequest.of(n,size,Sort.by("id").descending());}
 @PostMapping("/tickets") @ResponseStatus(HttpStatus.CREATED) View create(@Valid @RequestBody Create in,Authentication a){return View.of(service.create(in.title(),in.description(),in.priority(),a.getName()));}
 @GetMapping("/tickets/{id}") View get(@PathVariable long id,Authentication a){return View.of(service.visible(id,a));}
 @GetMapping("/tickets") PageView<View> list(@RequestParam(required=false) Ticket.Status status,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size,Authentication a){
  Pageable paging=page(page,size);Page<Ticket> result;
  if(TicketService.staff(a))result=status==null?tickets.findAll(paging):tickets.findByStatus(status,paging);
  else result=status==null?tickets.findByOwner(a.getName(),paging):tickets.findByOwnerAndStatus(a.getName(),status,paging);
  return new PageView<>(result.map(View::of).getContent(),page,size,result.getTotalElements());
 }
 @PostMapping("/tickets/{id}/claim") View claim(@PathVariable long id,@RequestBody java.util.Map<String,Object> ignored,Authentication a){return View.of(service.claim(id,a));}
 @PostMapping("/tickets/{id}/status") View status(@PathVariable long id,@Valid @RequestBody Change in,Authentication a){return View.of(service.transition(id,in.status(),a));}
 @PostMapping("/tickets/{id}/comments") @ResponseStatus(HttpStatus.NO_CONTENT) void comment(@PathVariable long id,@Valid @RequestBody Comment in,Authentication a){service.comment(id,in.text(),a);}
 @GetMapping("/tickets/{id}/events") PageView<EventView> events(@PathVariable long id,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size,Authentication a){service.visible(id,a);var p=events.findByTicketId(id,page(page,size));return new PageView<>(p.map(e->new EventView(e.id,e.actor,e.action,e.detail,e.happenedAt)).getContent(),page,size,p.getTotalElements());}
 record Metrics(long open,long inProgress,long resolved,long closed,long overdue){}
 @GetMapping("/reports/sla") Metrics metrics(Authentication a){TicketService.requireStaff(a);return new Metrics(tickets.countByStatus(Ticket.Status.OPEN),tickets.countByStatus(Ticket.Status.IN_PROGRESS),tickets.countByStatus(Ticket.Status.RESOLVED),tickets.countByStatus(Ticket.Status.CLOSED),tickets.overdue(Instant.now()));}
}
