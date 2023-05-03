package back.config;

import back.notification.application.domain.Notification;
import back.global.batch.NotificationAddIngredientConfig;
import back.notification.adapter.out.repository.NotificationRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.*;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBatchTest
@SpringBootTest(classes = {NotificationAddIngredientConfig.class, TestBatchLegacyConfig.class})
class NotificationAddIngredientConfigTest {

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
    void 식재료_추가_알림_테스트() throws Exception {

        //given
        Map<String, JobParameter> jobParameterMap = new HashMap<>();

        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:SSS");

        Long id = 2L;
        String name = "두부";

        jobParameterMap.put("date", new JobParameter(LocalDateTime.now().format(format)));
        jobParameterMap.put("id", new JobParameter(id));            // 수정
        jobParameterMap.put("name", new JobParameter(name));        // 수정

        JobParameters parameters = new JobParameters(jobParameterMap);

        JobParameters jobParameters = new JobParametersBuilder()
                .addJobParameters(parameters)
                .toJobParameters();

        //when
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        //then
        assertThat(jobExecution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);

        List<Notification> notificationList = notificationRepository.findAll();

        List<String> emailList = em.createQuery("select si.email from SuggestedIngredient si where si.name = :name group by si.email", String.class)
                .setParameter("name", name).getResultList();
        assertThat(notificationList.size()).isEqualTo(emailList.size());

        assertThat(notificationList.get(0).getMessage()).isEqualTo("회원님이 요청했던 " + name + "를 이제 냉장고에 담을 수 있습니다. (식재료 추가하기)");
        assertThat(notificationList.get(0).getPath()).isEqualTo("localhost:8080/api/ingredients/registered/" + id);
    }
}