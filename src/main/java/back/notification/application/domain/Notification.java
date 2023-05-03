package back.notification.application.domain;


import back.global.common.BaseTimeEntity;
import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "notification")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Notification extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long id;

    @Column(name = "message", nullable = false, length = 1000)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private NotificationType type;

    @Column(name = "path", nullable = false, length = 400)
    private String path;

    @Column(name = "read_status", nullable = false)
    private boolean readStatus; // true : 읽음 / false : 안 읽음

    @Column(name = "member_id", nullable = false)
    private String memberId;

    @Column(name = "method", nullable = false, length = 30)
    private String method;

    public static Notification create(NotificationType type, String path, String memberId, String method){
        return Notification.builder()
                .type(type)
                .path(path)
                .method(method)
                .memberId(memberId).build();
    }

    public String createExpirationDateMessage(String name, Long count, Integer days){
        return name + " 외 "+ count + "개 식재료의 소비기한이 " + days + "일 남았습니다. 식재료 확인하러가기!";
    }

    public String createIngredientMessage(String name) {
        return "회원님이 요청했던 " + name + "를 이제 냉장고에 담을 수 있습니다. (식재료 추가하기)";
    }

    public String createNoticeMessage(String title) {
        return "공지사항이 추가되었어요! '" + title + "'";
    }
}
