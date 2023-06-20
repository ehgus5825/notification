package back.config;

import back.entity.*;
import back.TestData;
import back.dto.OutIngredientDTO;
import back.notification.adapter.out.dto.QOutIngredientDTO;
import back.persistence.NotificationByMemberAdapter;
import back.entity.Notification;
import back.batch.NotificationScheduleConfig;
import back.repository.NotificationRepository;
import back.entity.NotificationType;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.*;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpMethod;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static back.entity.QIngredient.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBatchTest
@SpringBootTest(classes = {
        NotificationScheduleConfig.class,
        TestBatchLegacyConfig.class,
        NotificationByMemberAdapter.class,
        NotificationRedisConfig.class,
        TestData.class
})
class NotificationScheduleConfigTest extends BatchTestSupport {

    @Autowired
    @Qualifier("notificationRedisTemplate")
    private RedisTemplate<String, Boolean> notificationRedisTemplate;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private TestData testData;

    @Autowired
    private JPAQueryFactory jpaQueryFactory;

    private LocalDateTime now;
    private List<Ingredient> ingredients = new ArrayList<>();
    private Member member;

    @BeforeEach
    private void before() {
        member = testData.createMember("email01@gmail.com");
        notificationRedisTemplate.opsForValue().set(member.getEmail(), false);

        RegisteredIngredient registered1 = entityManager.find(RegisteredIngredient.class, 1L);
        RegisteredIngredient registered2 = entityManager.find(RegisteredIngredient.class, 2L);

        ingredients.add(testData.createIngredient(registered1.getName(), member.getEmail(), 1));
        ingredients.add(testData.createIngredient(registered2.getName(), member.getEmail(), 1));
        ingredients.add(testData.createIngredient(registered1.getName(), member.getEmail(), 3));

        entityManager.getTransaction().begin();
        save(member);
        saveAll(Arrays.asList(ingredients.toArray()));
        entityManager.getTransaction().commit();
    }

    @AfterEach
    public void after() {
        notificationRepository.deleteTestNotification(now);

        entityManager.getTransaction().begin();
        delete(member);
        deleteAll(Arrays.asList(ingredients.toArray()));
        entityManager.getTransaction().commit();
        entityManager.clear();
    }

    @Test
    void 자정에_진행되어야하는_작업_테스트() throws Exception {

        Map<String, JobParameter> jobParameterMap = new HashMap<>();

        now = LocalDateTime.now();

        jobParameterMap.put("date", new JobParameter(now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:SSS"))));

        JobParameters parameters = new JobParameters(jobParameterMap);

        JobParameters jobParameters = getUniqueParameterBuilder()
                .addJobParameters(parameters)
                .toJobParameters();

        JobExecution jobExecution = launchJob(jobParameters);

        //then
        assertThat(jobExecution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);

        List<Notification> notificationList = notificationRepository.findAll();

        assertThat(notificationList.size()).isEqualTo(2);
        assertThat(notificationRedisTemplate.opsForValue().get(notificationList.get(0).getMemberId())).isTrue();

        OutIngredientDTO ingredient1 = getOutIngredientDTO(1).get();

        assertThat(notificationList.get(0).getMemberId()).isEqualTo(ingredient1.getEmail());
        assertThat(notificationList.get(0).getMessage()).isEqualTo(ingredient1.getName() + " 외 "+ (ingredient1.getCount() - 1) + "개 식재료의 소비기한이 " + 1 + "일 남았습니다. 식재료 확인하러가기!");
        assertThat(notificationList.get(0).getPath()).isEqualTo("/notification/exp?day=1");
        assertThat(notificationList.get(0).getType()).isEqualTo(NotificationType.EXPIRATION_DATE);
        assertThat(notificationList.get(0).getMethod()).isEqualTo(HttpMethod.GET.name());

        OutIngredientDTO ingredient2 = getOutIngredientDTO(3).get();

        assertThat(notificationList.get(1).getMemberId()).isEqualTo(ingredient2.getEmail());
        assertThat(notificationList.get(1).getMessage()).isEqualTo(ingredient2.getName() + "의 소비기한이 " + 3 + "일 남았습니다. 식재료 확인하러가기!");
        assertThat(notificationList.get(1).getPath()).isEqualTo("/notification/exp?day=3");
        assertThat(notificationList.get(1).getType()).isEqualTo(NotificationType.EXPIRATION_DATE);
        assertThat(notificationList.get(1).getMethod()).isEqualTo(HttpMethod.GET.name());

    }

    private Optional<OutIngredientDTO> getOutIngredientDTO(Integer days) {

        OutIngredientDTO outIngredientDTO = jpaQueryFactory.select(new QOutIngredientDTO(
                        ingredient.email,
                        ingredient.name.min(),
                        ingredient.id.count()))
                .from(ingredient)
                .where(
                        ingredient.expirationDate.eq(LocalDate.now().plusDays(days)),
                        QMember.member.memberStatus.eq(MemberStatus.STEADY_STATUS)
                )
                .leftJoin(QMember.member).on(QMember.member.email.eq(ingredient.email))
                .groupBy(ingredient.email)
                .fetchOne();

        return Optional.ofNullable(outIngredientDTO);
    }
}

