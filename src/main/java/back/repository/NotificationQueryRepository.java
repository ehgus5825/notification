package back.repository;

import back.exception.BasicException;
import back.entity.NotificationType;
import back.exception.NotificationExceptionType;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.function.Supplier;

import static back.notification.application.domain.QNotification.*;

@Repository
@RequiredArgsConstructor
public class NotificationQueryRepository {

    private final JPAQueryFactory jpaQueryFactory;
    private final EntityManager em;

    @Transactional
    public void deleteNotification(LocalDateTime date) {

        jpaQueryFactory.delete(notification)
                .where(
                        notification.type.eq(NotificationType.EXPIRATION_DATE),
                        nullSafeBuilder(() -> notification.createDate.lt(date))
                )
                .execute();

        em.flush();
        em.clear();
    }

    @Transactional
    public void deleteDeadlineNotification(LocalDateTime date) {

        jpaQueryFactory.delete(notification)
                .where(
                        notification.type.ne(NotificationType.EXPIRATION_DATE),
                        nullSafeBuilder(() -> notification.createDate.lt(date))
                ).execute();

        em.flush();
        em.clear();
    }

    @Transactional
    public void deleteTestNotification(LocalDateTime date) {

        jpaQueryFactory.delete(notification)
                .where(
                        nullSafeBuilder(() -> notification.createDate.goe(date))
                ).execute();

        em.flush();
        em.clear();
    }

    public static BooleanBuilder nullSafeBuilder(Supplier<BooleanExpression> f) {
        try {
            return new BooleanBuilder(f.get());
        } catch (IllegalArgumentException e) {
            throw new BasicException(NotificationExceptionType.NOTIFICATION_DELETE_FAIL);
        }
    }
}
