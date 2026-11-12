package dev.portfolio;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.Customizer;
@Configuration
class SecurityConfig {
  @Bean UserDetailsService users(){
    var encoder=new BCryptPasswordEncoder();
    String password=System.getenv().getOrDefault("DEMO_PASSWORD","LearnJava!21");
    return new InMemoryUserDetailsManager(
      User.withUsername("admin").password("{bcrypt}"+encoder.encode(password)).roles("ADMIN","AGENT","USER").build(),
      User.withUsername("agent").password("{bcrypt}"+encoder.encode(password)).roles("AGENT","USER").build(),
      User.withUsername("alice").password("{bcrypt}"+encoder.encode(password)).roles("USER").build(),
      User.withUsername("bob").password("{bcrypt}"+encoder.encode(password)).roles("USER").build());
  }
  @Bean SecurityFilterChain filter(HttpSecurity http) throws Exception {
    // API-only, stateless Basic auth. No cookies; browser requests must carry an explicit JSON content type.
    return http.csrf(c->c.disable()).sessionManagement(s->s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
      .authorizeHttpRequests(a->a.requestMatchers("/","/index.html","/app.js","/style.css","/actuator/health").permitAll().anyRequest().authenticated())
      .httpBasic(Customizer.withDefaults()).build();
  }
}
