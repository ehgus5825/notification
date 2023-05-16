package back.global.batch;

import back.notification.adapter.out.persistence.NotificationByMemberAdapter;
import back.notification.adapter.out.repository.NotificationRepository;
import back.notification.application.domain.Notification;
import back.notification.adapter.out.dto.OutIngredientDTO;
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
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;

import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

import javax.persistence.EntityManagerFactory;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

// 자정마다 해야할 것

@RequiredArgsConstructor
@Configuration
@Slf4j
public class NotificationScheduleConfig {

    private final EntityManagerFactory entityManagerFactory;
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final NotificationRepository notificationRepository;
    private final NotificationByMemberAdapter adapter;

    @Value("${chunkSize:1000}")
    private int chunkSize = 1000;

    @Bean
    public Job scheduleJob(){
        return jobBuilderFactory.get("scheduleJob")
                .preventRestart()
                .start(deleteNotificationStep())
                .next(createDeadlineNotificationByOneStep())
                .next(createDeadlineNotificationByThreeStep())
                .build();
    }

    // 알림 삭제 Step
    @Bean
    @JobScope
    public Step deleteNotificationStep() {

        return stepBuilderFactory.get("deleteNotificationStep")
                .tasklet((contribution, chunkContext) -> {

                    notificationRepository.deleteNotification(LocalDateTime.now());
                    notificationRepository.deleteDeadlineNotification(LocalDateTime.now().minusDays(14));
                    
                    return RepeatStatus.FINISHED;
                })
                .build();
    }

    // 임박 식재료 알림 생성 Step (1일)
    @Bean
    @JobScope
    public Step createDeadlineNotificationByOneStep() {
        return stepBuilderFactory.get("createDeadlineNotificationStep")
                .<OutIngredientDTO, Notification> chunk(chunkSize)
                .reader(createDeadlineNotificationByOneReader(null))
                .processor(createDeadlineNotificationByOneProcessor())
                .writer(createDeadlineNotificationWriter())
                .build();
    }

    @Bean
    @StepScope
    public JpaPagingItemReader<OutIngredientDTO> createDeadlineNotificationByOneReader(
                                                                        @Value("#{jobParameters['date']}") String date) {

        LocalDate date1 = LocalDate.from(LocalDateTime.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:SSS")));

        Map<String, Object> parameterValues = new HashMap<>();
        parameterValues.put("date", date1.plusDays(1));

        return new JpaPagingItemReaderBuilder<OutIngredientDTO>()
                .name("createDeadlineNotificationReader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(chunkSize)
                .queryString("select new back.notification.adapter.out.dto.OutIngredientDTO(i.email, min(i.name) as name, count(i.id) as ingredient_count) from Ingredient i where i.expirationDate = :date group by i.email")
                .parameterValues(parameterValues)
                .build();
    }

    @Bean
    @StepScope
    public ItemProcessor<OutIngredientDTO, Notification> createDeadlineNotificationByOneProcessor() {

        return (dto) -> {
            adapter.update(dto.getEmail());

            Notification notification = Notification.create(
                    NotificationType.EXPIRATION_DATE,
                    "/api/ingredients/deadline/1",
                    dto.getEmail(),
                    HttpMethod.GET.name());
            notification.createExpirationDateMessage(dto.getName(), dto.getCount(), 1);
            return notification;
        };
    }

    // 임박 식재료 알림 생성 Step (3일)
    @Bean
    @JobScope
    public Step createDeadlineNotificationByThreeStep() {
        return stepBuilderFactory.get("createDeadlineNotificationStep")
                .<OutIngredientDTO, Notification> chunk(chunkSize)
                .reader(createDeadlineNotificationByThreeReader(null))
                .processor(createDeadlineNotificationByThreeProcessor())
                .writer(createDeadlineNotificationWriter())
                .build();
    }

    @Bean
    @StepScope
    public JpaPagingItemReader<OutIngredientDTO> createDeadlineNotificationByThreeReader(
                                                                        @Value("#{jobParameters['date']}") String date) {

        LocalDate date1 = LocalDate.from(LocalDateTime.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:SSS")));

        Map<String, Object> parameterValues = new HashMap<>();
        parameterValues.put("date", date1.plusDays(3));

        return new JpaPagingItemReaderBuilder<OutIngredientDTO>()
                .name("createDeadlineNotificationReader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(chunkSize)
                .queryString("select new back.notification.adapter.out.dto.OutIngredientDTO(i.email, min(i.name) as name, count(i.id) as ingredient_count) from Ingredient i where i.expirationDate = :date group by i.email")
                .parameterValues(parameterValues)
                .build();
    }

    @Bean
    @StepScope
    public ItemProcessor<OutIngredientDTO, Notification> createDeadlineNotificationByThreeProcessor() {

        return (dto) -> {
            adapter.update(dto.getEmail());
            Notification notification = Notification.create(
                    NotificationType.EXPIRATION_DATE,
                    "/api/ingredients/deadline/3",
                    dto.getEmail(),
                    HttpMethod.GET.name());
            notification.createExpirationDateMessage(dto.getName(), dto.getCount() - 1, 3);
            return notification;
        };
    }

    @Bean
    @StepScope
    public JpaItemWriter<Notification> createDeadlineNotificationWriter() {
        JpaItemWriter<Notification> jpaItemWriter = new JpaItemWriter<>();
        jpaItemWriter.setEntityManagerFactory(entityManagerFactory);
        return jpaItemWriter;
    }
}
