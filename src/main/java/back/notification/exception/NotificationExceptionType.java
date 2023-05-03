package back.notification.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum NotificationExceptionType {

    NOTIFICATION_SEND_FAIL("NOTIFICATION_SEND_FAIL", "알림 전송 실패.", HttpStatus.BAD_REQUEST)
    ;

    String errorCode;
    String message;
    HttpStatus httpStatus;

}
