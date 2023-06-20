package back.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum NotificationType {

    HEART("좋아요"),
    NOTICE("공지사항"),
    INGREDIENT("식재료"),
    EXPIRATION_DATE("유통기한")
    ;

    private final String typeName;
}
