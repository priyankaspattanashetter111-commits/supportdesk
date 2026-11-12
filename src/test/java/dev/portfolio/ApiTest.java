package dev.portfolio;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.*;
import org.springframework.http.MediaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest @AutoConfigureMockMvc
class ApiTest {
 @Autowired MockMvc mvc; @Autowired ObjectMapper mapper;
 ResultActions postAs(String uri,String json,String user)throws Exception{return mvc.perform(post(uri).with(httpBasic(user,"LearnJava!21")).contentType(MediaType.APPLICATION_JSON).content(json));}
 ResultActions getAs(String uri,String user)throws Exception{return mvc.perform(get(uri).with(httpBasic(user,"LearnJava!21")));}
 long id(ResultActions result)throws Exception{return mapper.readTree(result.andReturn().getResponse().getContentAsString()).get("id").asLong();}

 @Autowired Tickets tickets;
 String ticket="{\"title\":\"VPN fails\",\"description\":\"Cannot connect from home\",\"priority\":\"HIGH\"}";
 long create()throws Exception{return id(postAs("/api/tickets",ticket,"alice").andExpect(status().isCreated()));}
 @Test void authenticationRequired()throws Exception{mvc.perform(get("/api/tickets")).andExpect(status().isUnauthorized());}
 @Test void creationSetsEightHourSlaAndAudit()throws Exception{
  long t=create();var stored=tickets.findById(t).orElseThrow();assertEquals(java.time.Duration.ofHours(8),java.time.Duration.between(stored.createdAt,stored.dueAt));
  getAs("/api/tickets/"+t+"/events","alice").andExpect(jsonPath("$.content[0].action").value("CREATED"));
 }
 @Test void otherUserCannotReadOrComment()throws Exception{
  long t=create();getAs("/api/tickets/"+t,"bob").andExpect(status().isNotFound());
  postAs("/api/tickets/"+t+"/comments","{\"text\":\"hi\"}","bob").andExpect(status().isNotFound());
  getAs("/api/tickets/"+t+"/events","bob").andExpect(status().isNotFound());
 }
 @Test void onlyStaffCanClaimAndClaimsCannotBeStolen()throws Exception{
  long t=create();postAs("/api/tickets/"+t+"/claim","{}","alice").andExpect(status().isForbidden());
  postAs("/api/tickets/"+t+"/claim","{}","agent").andExpect(jsonPath("$.assignee").value("agent"));
  postAs("/api/tickets/"+t+"/claim","{}","admin").andExpect(status().isConflict());
 }
 @Test void workflowRequiresClaimAndPreventsSkipping()throws Exception{
  long t=create();String base="/api/tickets/"+t;
  postAs(base+"/status","{\"status\":\"IN_PROGRESS\"}","agent").andExpect(status().isForbidden());
  postAs(base+"/claim","{}","agent");
  postAs(base+"/status","{\"status\":\"RESOLVED\"}","agent").andExpect(status().isConflict());
  postAs(base+"/status","{\"status\":\"IN_PROGRESS\"}","agent").andExpect(status().isOk());
  postAs(base+"/status","{\"status\":\"RESOLVED\"}","agent").andExpect(jsonPath("$.resolvedAt").isNotEmpty());
  postAs(base+"/status","{\"status\":\"CLOSED\"}","agent").andExpect(status().isForbidden());
  postAs(base+"/status","{\"status\":\"CLOSED\"}","alice").andExpect(status().isOk());
  postAs(base+"/comments","{\"text\":\"after close\"}","alice").andExpect(status().isConflict());
 }
 @Test void commentsAreAudited()throws Exception{
  long t=create();postAs("/api/tickets/"+t+"/comments","{\"text\":\"Attached details\"}","alice").andExpect(status().isNoContent());
  getAs("/api/tickets/"+t+"/events","agent").andExpect(jsonPath("$.content[0].detail").value("Attached details"));
 }
 @Test void invalidPriorityAndPaginationRejected()throws Exception{
  postAs("/api/tickets",ticket.replace("HIGH","INVALID"),"alice").andExpect(status().isBadRequest());
  postAs("/api/tickets",ticket.replace("VPN fails",""),"alice").andExpect(status().isBadRequest());
  getAs("/api/tickets?page=-1","agent").andExpect(status().isBadRequest());
 }
 @Test void reportIsStaffOnlyAndCountsOverdue()throws Exception{
  long id=create();Ticket t=tickets.findById(id).orElseThrow();t.dueAt=java.time.Instant.now().minusSeconds(60);tickets.save(t);
  getAs("/api/reports/sla","alice").andExpect(status().isForbidden());
  getAs("/api/reports/sla","agent").andExpect(status().isOk()).andExpect(jsonPath("$.overdue").value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)));
 }
 @Test void requesterListNeverIncludesOtherOwners()throws Exception{
  long id=id(postAs("/api/tickets",ticket,"bob"));
  var rows=mapper.readTree(getAs("/api/tickets?size=100","alice").andReturn().getResponse().getContentAsString()).get("content");
  for(var row:rows)assertNotEquals(id,row.get("id").asLong());
  getAs("/api/tickets?status=OPEN","agent").andExpect(status().isOk());
 }

}
