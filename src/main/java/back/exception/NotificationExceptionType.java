package back.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum NotificationExceptionType implements BasicExceptionType {

    NOTICE_NOTIFICATION_CREATE_FAIL("NOTICE_NOTIFICATION_CREATE_FAIL", "공지사항 알림 생성 실패.", HttpStatus.BAD_REQUEST),
    ADD_INGREDIENT_NOTIFICATION_CREATE_FAIL("ADD_INGREDIENT_NOTIFICATION_CREATE_FAIL", "식재료 추가 알림 생성 실패.", HttpStatus.BAD_REQUEST),
    NOTIFICATION_CREATE_FAIL("NOTIFICATION_CREATE_FAIL", "알림 생성 실패.", HttpStatus.BAD_REQUEST),
    NOTIFICATION_DELETE_FAIL("NOTIFICATION_DELETE_FAIL", "알림 삭제 실패.", HttpStatus.BAD_REQUEST)
    ;

    String errorCode;
    String message;
    HttpStatus httpStatus;

}
