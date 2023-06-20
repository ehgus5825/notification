package back.etc;

import back.entity.Notification;
import back.entity.NotificationType;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.function.Supplier;

import static back.notification.application.domain.QNotification.*;
import static org.springframework.util.StringUtils.hasText;

@Transactional
@SpringBootTest
public class nullExpressionTest {

    @Autowired
    JPAQueryFactory jpaQueryFactory;

    //@Autowired
    //EntityManager entityManager;

    @Test
    public void check() {

        LocalDateTime dateTime = null;

        Notification notification1 = Notification.create(
                NotificationType.NOTICE,
                "/api/notice/" + 1,
                "ehgus5825@naver.com",
                HttpMethod.GET.name());
        notification1.createNoticeMessage("fwqfqwfwqfqwfwq");

        //entityManager.persist(notification1);

        BooleanBuilder booleanBuilder = nullSafeBuilder(() -> notification.createDate.goe(dateTime));

        System.out.println("booleanBuilder = " + booleanBuilder);

        jpaQueryFactory.selectFrom(notification)
                .where(nullSafeBuilder(() -> notification.createDate.goe(dateTime)))
                .fetch();
    }

    public static BooleanBuilder nullSafeBuilder(Supplier<BooleanExpression> f) {
        try {
            return new BooleanBuilder(f.get());
        } catch (NullPointerException e) {
            return new BooleanBuilder();
        }
    }
}