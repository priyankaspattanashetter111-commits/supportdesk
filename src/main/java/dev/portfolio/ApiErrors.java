package dev.portfolio;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
@RestControllerAdvice
class ApiErrors {
 @ExceptionHandler(ResponseStatusException.class) ResponseEntity<ProblemDetail> known(ResponseStatusException e){
  return ResponseEntity.status(e.getStatusCode()).body(ProblemDetail.forStatusAndDetail(e.getStatusCode(),e.getReason()==null?"Request failed":e.getReason()));
 }
 @ExceptionHandler({MethodArgumentNotValidException.class,HttpMessageNotReadableException.class,MethodArgumentTypeMismatchException.class})
 ResponseEntity<ProblemDetail> invalid(Exception e){return ResponseEntity.badRequest().body(ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,"Invalid request. Check required fields, types and allowed values."));}
 @ExceptionHandler(DataIntegrityViolationException.class) ResponseEntity<ProblemDetail> conflict(Exception e){return ResponseEntity.status(409).body(ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,"Conflicting record; refresh and retry."));}
}
