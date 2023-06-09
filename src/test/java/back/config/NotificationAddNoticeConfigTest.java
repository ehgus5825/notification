package back.config;

import back.TestData;
import back.batch.NotificationAddNoticeConfig;
import back.entity.Member;
import back.entity.Notice;
import back.persistence.NotificationByMemberAdapter;
import back.entity.Notification;
import back.repository.NotificationRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.*;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

@SpringBatchTest
@SpringBootTest(classes = {
        NotificationAddNoticeConfig.class,
        TestBatchLegacyConfig.class,
        NotificationByMemberAdapter.class,
        NotificationRedisConfig.class,
        TestData.class
})
class NotificationAddNoticeConfigTest extends BatchTestSupport {

    @Autowired
    @Qualifier("notificationRedisTemplate")
    private RedisTemplate<String, Boolean> notificationRedisTemplate;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private TestData testData;

    private Member member1;
    private Member member2;
    private Notice notice;
    private LocalDateTime now;

    @BeforeEach
    private void before() {
        //given
        entityManager.getTransaction().begin();

        member1 = testData.createMember("email01@gmail.com");
        notice = testData.createNotice("안녕하세요");
        notificationRedisTemplate.opsForValue().set(member1.getEmail(), false);

        save(member1);
        save(notice);

        entityManager.getTransaction().commit();
    }

    @AfterEach
    public void after() {
        entityManager.getTransaction().begin();

        notificationRepository.deleteTestNotification(now);
        delete(member1);
        delete(notice);

        entityManager.getTransaction().commit();
        entityManager.clear();
    }

    @Test
    void 공지사항_알림_테스트() throws Exception {
        //when
        Map<String, JobParameter> jobParameterMap = new HashMap<>();

        now = LocalDateTime.now();

        jobParameterMap.put("date", new JobParameter(now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:SSS"))));
        jobParameterMap.put("id", new JobParameter(notice.getId()));
        jobParameterMap.put("title", new JobParameter(notice.getTitle()));

        JobParameters parameters = new JobParameters(jobParameterMap);

        JobParameters jobParameters = getUniqueParameterBuilder()
                .addJobParameters(parameters)
                .toJobParameters();

        JobExecution jobExecution = launchJob(jobParameters);

        //then
        assertThat(jobExecution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);

        Notification notification = notificationRepository.findAll().stream().findAny().get();

        assertThat(notification.getMemberId()).isEqualTo(member1.getEmail());
        assertThat(notificationRedisTemplate.opsForValue().get(member1.getEmail())).isTrue();

        assertThat(notification.getMessage()).isEqualTo("공지사항이 추가되었어요! '" + notice.getTitle() + "'");
        assertThat(notification.getPath()).isEqualTo("/api/notice/" + notice.getId());

    }
}