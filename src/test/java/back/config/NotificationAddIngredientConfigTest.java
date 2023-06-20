package back.config;

import back.TestData;
import back.entity.RegisteredIngredient;
import back.entity.SuggestedIngredient;
import back.entity.Member;
import back.persistence.NotificationByMemberAdapter;
import back.entity.Notification;
import back.batch.NotificationAddIngredientConfig;
import back.repository.NotificationRepository;
import back.entity.NotificationType;
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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBatchTest
@SpringBootTest(classes = {
        NotificationAddIngredientConfig.class,
        TestBatchLegacyConfig.class,
        NotificationByMemberAdapter.class,
        NotificationRedisConfig.class,
        TestData.class
})
class NotificationAddIngredientConfigTest extends BatchTestSupport {

    @Autowired
    @Qualifier("notificationRedisTemplate")
    private RedisTemplate<String, Boolean> notificationRedisTemplate;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private TestData testData;

    private Member member;
    private SuggestedIngredient suggestedIngredient;
    private RegisteredIngredient registeredIngredient;
    private LocalDateTime now;

    @BeforeEach
    private void before() {
        //given
        entityManager.getTransaction().begin();

        registeredIngredient = entityManager.find(RegisteredIngredient.class, 1L);
        member = testData.createMember("email01@gmail.com");
        suggestedIngredient = testData.createSuggestedIngredient("email01@gmail.com", registeredIngredient.getName());
        notificationRedisTemplate.opsForValue().set(suggestedIngredient.getEmail(), false);

        save(member);
        save(suggestedIngredient);

        entityManager.getTransaction().commit();
    }

    @AfterEach
    public void after() {
        entityManager.getTransaction().begin();

        notificationRepository.deleteTestNotification(now);
        delete(member);
        //delete(suggestedIngredient);

        entityManager.getTransaction().commit();
        entityManager.clear();
    }

    @Test
    void 식재료_추가_알림_테스트() throws Exception {

        //when
        Map<String, JobParameter> jobParameterMap = new HashMap<>();

        now = LocalDateTime.now();

        jobParameterMap.put("date", new JobParameter(now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:SSS"))));
        jobParameterMap.put("id", new JobParameter(registeredIngredient.getId()));
        jobParameterMap.put("name", new JobParameter(registeredIngredient.getName()));

        JobParameters parameters = new JobParameters(jobParameterMap);

        JobParameters jobParameters = getUniqueParameterBuilder()
                .addJobParameters(parameters)
                .toJobParameters();

        JobExecution jobExecution = launchJob(jobParameters);

        //then
        assertThat(jobExecution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);


        Notification notification = notificationRepository.findAll().stream().findAny().get();

        assertThat(notificationRedisTemplate.opsForValue().get(suggestedIngredient.getEmail())).isTrue();

        assertThat(notification.getMemberId()).isEqualTo(suggestedIngredient.getEmail());
        assertThat(notification.getMessage()).isEqualTo("회원님이 요청했던 " + registeredIngredient.getName() + "를 이제 냉장고에 담을 수 있습니다. (식재료 추가하기)");
        assertThat(notification.getPath()).isEqualTo("/refrigerator/add/info?ingredient=" + registeredIngredient.getName());
        assertThat(notification.getType()).isEqualTo(NotificationType.INGREDIENT);
        assertThat(notification.getMethod()).isEqualTo(HttpMethod.GET.name());
    }
}