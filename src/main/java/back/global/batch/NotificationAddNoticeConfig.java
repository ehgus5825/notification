package back.global.batch;

import back.notification.adapter.out.persistence.NotificationByMemberAdapter;
import back.notification.application.domain.Notification;
import back.notification.application.domain.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.*;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

import javax.persistence.EntityManagerFactory;

@RequiredArgsConstructor
@Configuration
@Slf4j
public class NotificationAddNoticeConfig {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final EntityManagerFactory entityManagerFactory;

    private final NotificationByMemberAdapter adapter;

    @Value("${chunkSize:1000}")
    private int chunkSize = 1000;

    @Bean
    public Job noticeJob(){
        return jobBuilderFactory.get("noticeJob")
                .preventRestart()
                .start(noticeStep())
                .build();
    }

    @Bean
    @JobScope
    public Step noticeStep() {
        return stepBuilderFactory.get("noticeStep")
                .<String, Notification>chunk(chunkSize)
                .reader(noticeNotificationReader())
                .processor(noticeNotificationProcessor(null, null))
                .writer(noticeNotificationWriter())
                .build();
    }

    @Bean
    @StepScope
    public JpaPagingItemReader<String> noticeNotificationReader() {
        return new JpaPagingItemReaderBuilder<String>()
                .name("noticeNotificationReader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(chunkSize)
                .queryString("select m.email from Member m")
                .build();

    }

    @Bean
    @StepScope
    public ItemProcessor<String, Notification> noticeNotificationProcessor(
                                                                    @Value("#{jobParameters['title']}") String title,
                                                                    @Value("#{jobParameters['id']}") Long id ) {

        return email -> {
            adapter.create(email);
            Notification notification = Notification.create(
                    NotificationType.NOTICE,
                    "/api/notice/" + id,
                    email,
                    HttpMethod.GET.name());
            notification.createNoticeMessage(title);
            return notification;
        };
    }

    @Bean
    @StepScope
    public JpaItemWriter<Notification> noticeNotificationWriter() {
        JpaItemWriter<Notification> jpaItemWriter = new JpaItemWriter<>();
        jpaItemWriter.setEntityManagerFactory(entityManagerFactory);
        return jpaItemWriter;
    }
}
