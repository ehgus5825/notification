package back.global.exception;

import back.notification.exception.NotificationExceptionType;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ExceptionFormat {

    private String code;
    private String message;

    public static ExceptionFormat create(NotificationExceptionType exceptionType){
        return new ExceptionFormat(
                exceptionType.getErrorCode(),
                exceptionType.getMessage()
        );
    }
}
