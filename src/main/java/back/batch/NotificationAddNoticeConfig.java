package back.batch;

import back.common.querydsl.expression.Expression;
import back.common.querydsl.options.QuerydslNoOffsetNumberOptions;
import back.common.querydsl.reader.QuerydslNoOffsetPagingItemReader;
import back.entity.Member;
import back.entity.MemberStatus;
import back.persistence.NotificationByMemberAdapter;
import back.entity.Notification;
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
import org.springframework.batch.item.database.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

import javax.persistence.EntityManagerFactory;

import static back.entity.QMember.*;


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
                .<Member, Notification>chunk(chunkSize)
                .reader(noticeNotificationReader())
                .processor(noticeNotificationProcessor(null, null))
                .writer(noticeNotificationWriter())
                .build();
    }

    @Bean
    @StepScope
    public QuerydslNoOffsetPagingItemReader<Member> noticeNotificationReader() {

        QuerydslNoOffsetNumberOptions<Member, Long> option = new QuerydslNoOffsetNumberOptions<>(member.id, Expression.ASC);

        return new QuerydslNoOffsetPagingItemReader<>(entityManagerFactory, chunkSize, option,
                queryFactory -> queryFactory
                        .selectFrom(member)
                        .where(member.memberStatus
                                .eq(MemberStatus.STEADY_STATUS)));
    }

    @Bean
    @StepScope
    public ItemProcessor<Member, Notification> noticeNotificationProcessor(
                                                                    @Value("#{jobParameters['title']}") String title,
                                                                    @Value("#{jobParameters['id']}") Long id ) {

        return member -> {
            adapter.update(member.getEmail());
            Notification notification = Notification.create(
                    NotificationType.NOTICE,
                    "/api/notice/" + id,
                    member.getEmail(),
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
