package back.config;

import back.global.batch.NotificationAddNoticeConfig;
import back.member.application.domain.Member;
import back.notification.application.domain.Notification;
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

import static org.assertj.core.api.Assertions.*;

@SpringBatchTest
@SpringBootTest(classes = {TestBatchLegacyConfig.class, NotificationAddNoticeConfig.class})
class NotificationAddNoticeConfigTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private NotificationAddNoticeConfig noticeConfig;

    @Autowired
    private NotificationRepository notificationRepository;

    @AfterEach
    public void reset() {
        notificationRepository.deleteAllInBatch();
    }

    @Autowired
    private EntityManager em;

    @Test
    void 공지사항_알림_테스트() throws Exception {

        //given
        Map<String, JobParameter> jobParameterMap = new HashMap<>();

        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:SSS");

        Long id = 1L;
        String title = "문자열";
        
        jobParameterMap.put("date", new JobParameter(LocalDateTime.now().format(format)));
        jobParameterMap.put("id", new JobParameter(id));            // 수정
        jobParameterMap.put("title", new JobParameter(title));    // 수정

        JobParameters parameters = new JobParameters(jobParameterMap);

        JobParameters jobParameters = new JobParametersBuilder()
                .addJobParameters(parameters)
                .toJobParameters();

        //when
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        //then
        assertThat(jobExecution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);

        List<Notification> notificationList = notificationRepository.findAll();


        List<Member> memberList = em.createQuery("select m from Member m", Member.class).getResultList(); // where m.memberStatus = jpql.MemberStatus.STEADY_STATUS
        assertThat(notificationList.size()).isEqualTo(memberList.size());

        assertThat(notificationList.get(0).getMessage()).isEqualTo("공지사항이 추가되었어요! '" + title + "'");
        assertThat(notificationList.get(0).getPath()).isEqualTo("localhost:8080/api/notice/" + id);
    }
}