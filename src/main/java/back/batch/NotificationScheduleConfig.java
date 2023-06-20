package back.batch;

import back.common.querydsl.expression.Expression;
import back.common.querydsl.options.QuerydslNoOffsetStringOptions;
import back.common.querydsl.reader.QuerydslNoOffsetPagingItemReader;
import back.common.querydsl.reader.QuerydslPagingItemReader;
import back.entity.MemberStatus;
import back.notification.adapter.out.dto.QOutIngredientDTO;
import back.persistence.NotificationByMemberAdapter;
import back.repository.NotificationRepository;
import back.entity.Notification;
import back.dto.OutIngredientDTO;
import back.entity.NotificationType;
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

import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

import javax.persistence.EntityManagerFactory;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static back.entity.QIngredient.*;
import static back.entity.QMember.member;
import static java.time.LocalDate.*;
import static java.time.format.DateTimeFormatter.ofPattern;

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
    public QuerydslPagingItemReader<OutIngredientDTO> createDeadlineNotificationByOneReader(
                                                                        @Value("#{jobParameters['date']}") String date) {

        LocalDate localDate = from(LocalDateTime.parse(date, ofPattern("yyyy-MM-dd HH:mm:ss:SSS")));

        QuerydslNoOffsetStringOptions<OutIngredientDTO> options = new QuerydslNoOffsetStringOptions<>(ingredient.email, Expression.ASC);

        return new QuerydslNoOffsetPagingItemReader<>(entityManagerFactory, chunkSize, options,
                queryFactory -> queryFactory
                        .select(new QOutIngredientDTO(
                                ingredient.email,
                                ingredient.name.min(),
                                ingredient.id.count())
                        )
                        .from(ingredient)
                        .where(ingredient.expirationDate.eq(localDate.plusDays(1)),
                                member.memberStatus.eq(MemberStatus.STEADY_STATUS))
                        .leftJoin(member).on(member.email.eq(ingredient.email))
                        .groupBy(ingredient.email));
    }

    @Bean
    @StepScope
    public ItemProcessor<OutIngredientDTO, Notification> createDeadlineNotificationByOneProcessor() {

        return (dto) -> {

            adapter.update(dto.getEmail());

            Notification notification = Notification.create(
                    NotificationType.EXPIRATION_DATE,
                    "/notification/exp?day=1",
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
    public QuerydslNoOffsetPagingItemReader<OutIngredientDTO> createDeadlineNotificationByThreeReader(
                                                                        @Value("#{jobParameters['date']}") String date) {

        LocalDate localDate = from(LocalDateTime.parse(date, ofPattern("yyyy-MM-dd HH:mm:ss:SSS")));

        QuerydslNoOffsetStringOptions<OutIngredientDTO> options = new QuerydslNoOffsetStringOptions<>(ingredient.email, Expression.ASC);

        return new QuerydslNoOffsetPagingItemReader<>(entityManagerFactory, chunkSize, options,
                queryFactory -> queryFactory
                        .select(new QOutIngredientDTO(
                                ingredient.email,
                                ingredient.name.min(),
                                ingredient.id.count())
                        )
                        .from(ingredient)
                        .where(ingredient.expirationDate.eq(localDate.plusDays(3)),
                                member.memberStatus.eq(MemberStatus.STEADY_STATUS))
                        .leftJoin(member).on(member.email.eq(ingredient.email))
                        .groupBy(ingredient.email));
    }

    @Bean
    @StepScope
    public ItemProcessor<OutIngredientDTO, Notification> createDeadlineNotificationByThreeProcessor() {

        return (dto) -> {
            adapter.update(dto.getEmail());
            Notification notification = Notification.create(
                    NotificationType.EXPIRATION_DATE,
                    "/notification/exp?day=3",
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
