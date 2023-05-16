package back.global.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class BasicExceptionHandler {

    @ExceptionHandler(BasicException.class)
    public ResponseEntity<BasicExceptionFormat> BasicExceptionHandler(BasicException e){
        return new ResponseEntity<>(
                BasicExceptionFormat.create(e.getExceptionType()),
                e.getExceptionType().getHttpStatus()
        );
    }
}
