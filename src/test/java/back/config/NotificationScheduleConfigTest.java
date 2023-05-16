package back.config;

import back.global.TestData;
import back.global.batch.NotificationAddNoticeConfig;
import back.global.config.NotificationRedisConfig;
import back.ingredient.application.domain.Ingredient;
import back.ingredient.application.domain.RegisteredIngredient;
import back.member.application.domain.Member;
import back.notification.adapter.out.dto.OutIngredientDTO;
import back.notification.adapter.out.persistence.NotificationByMemberAdapter;
import back.notification.application.domain.Notification;
import back.global.batch.NotificationScheduleConfig;
import back.notification.adapter.out.repository.NotificationRepository;
import back.notification.application.domain.NotificationType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.*;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.ContextConfiguration;

import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

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

        String query = "select new back.notification.adapter.out.dto." +
                "OutIngredientDTO(i.email, min(i.name) as name, count(i.id) as ingredient_count) " +
                "from Ingredient i " +
                "where i.expirationDate = :date " +
                "group by i.email";

        OutIngredientDTO ingredient1 = entityManager.createQuery(query, OutIngredientDTO.class)
                .setParameter("date", LocalDate.now().plusDays(1))
                .getResultList().stream().findAny().get();

        assertThat(notificationList.get(0).getMemberId()).isEqualTo(ingredient1.getEmail());
        assertThat(notificationList.get(0).getMessage()).isEqualTo(ingredient1.getName() + " 외 "+ (ingredient1.getCount() - 1) + "개 식재료의 소비기한이 " + 1 + "일 남았습니다. 식재료 확인하러가기!");
        assertThat(notificationList.get(0).getPath()).isEqualTo("/api/ingredients/deadline/" + 1);
        assertThat(notificationList.get(0).getType()).isEqualTo(NotificationType.EXPIRATION_DATE);
        assertThat(notificationList.get(0).getMethod()).isEqualTo(HttpMethod.GET.name());

        OutIngredientDTO ingredient2 = entityManager.createQuery(query, OutIngredientDTO.class)
                .setParameter("date", LocalDate.now().plusDays(3))
                .getResultList().stream().findAny().get();

        assertThat(notificationList.get(1).getMemberId()).isEqualTo(ingredient2.getEmail());
        assertThat(notificationList.get(1).getMessage()).isEqualTo(ingredient2.getName() + "의 소비기한이 " + 3 + "일 남았습니다. 식재료 확인하러가기!");
        assertThat(notificationList.get(1).getPath()).isEqualTo("/api/ingredients/deadline/" + 3);
        assertThat(notificationList.get(1).getType()).isEqualTo(NotificationType.EXPIRATION_DATE);
        assertThat(notificationList.get(1).getMethod()).isEqualTo(HttpMethod.GET.name());

    }
}