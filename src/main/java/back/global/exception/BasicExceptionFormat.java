package back.global.exception;

import back.notification.exception.NotificationExceptionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BasicExceptionFormat {

    private String code;
    private String message;

    public static BasicExceptionFormat create(BasicExceptionType exceptionType){
        return new BasicExceptionFormat(
                exceptionType.getErrorCode(),
                exceptionType.getMessage()
        );
    }
}
