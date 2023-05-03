package back.config;

import back.notification.application.domain.Notification;
import back.global.batch.NotificationScheduleConfig;
import back.notification.adapter.out.repository.NotificationRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.*;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBatchTest
@SpringBootTest(classes = {NotificationScheduleConfig.class, TestBatchLegacyConfig.class})
class NotificationScheduleConfigTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private EntityManager em;


    @AfterEach
    public void reset() {
        notificationRepository.deleteAllInBatch();
    }

    @Test
    void 자정에_진행되어야하는_작업_테스트() throws Exception {

        //given
        Map<String, JobParameter> jobParameterMap = new HashMap<>();

        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:SSS");

        jobParameterMap.put("date", new JobParameter(LocalDateTime.now().format(format)));

        JobParameters parameters = new JobParameters(jobParameterMap);

        JobParameters jobParameters = new JobParametersBuilder()
                .addJobParameters(parameters)
                .toJobParameters();

        //when
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        //then
        assertThat(jobExecution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);

        List<Notification> notificationList = notificationRepository.findAll();

        List list1 = em.createQuery("select i.email, min(i.name) as name, count(i.id) as ingredient_count from Ingredient i where i.expirationDate = :date group by i.email")
                .setParameter("date", LocalDate.now().plusDays(1))
                .getResultList();

        List list2 = em.createQuery("select i.email, min(i.name) as name, count(i.id) as ingredient_count from Ingredient i where i.expirationDate = :date group by i.email")
                .setParameter("date", LocalDate.now().plusDays(3))
                .getResultList();

        assertThat(notificationList.size()).isEqualTo(list1.size() + list2.size());

        for (Notification notification : notificationList) {
            assertThat(notification.getId()).isNotNull();
            assertThat(notification.getMessage()).isNotNull();
            assertThat(notification.getType()).isNotNull();
            assertThat(notification.getPath()).isNotNull();
            assertThat(notification.getMethod()).isNotNull();
        }
    }
}