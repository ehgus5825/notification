package back.global.exception;

import org.springframework.http.HttpStatus;

public interface BasicExceptionType {

    String getErrorCode();
    String getMessage();
    HttpStatus getHttpStatus();
}
