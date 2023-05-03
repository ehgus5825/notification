package back.global.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ExceptionHandler {

    @org.springframework.web.bind.annotation.ExceptionHandler(NotificationException.class)
    public ResponseEntity<ExceptionFormat> notificationException(NotificationException e){
        return new ResponseEntity<>(
                ExceptionFormat.create(e.getExceptionType()),
                e.getExceptionType().getHttpStatus()
        );
    }
}
