package back.notice;

import back.global.common.BaseTimeEntity;
import back.notification.application.domain.NotificationType;
import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "notice")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Notice extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notice_id")
    private Long id;

    @Column(name = "title", nullable = false, length = 400)
    private String title;

    @Lob
    @Column(name = "content", nullable = false)
    private String content;

}
