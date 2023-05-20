package back.global.batch;

import back.global.common.querydsl.expression.Expression;
import back.global.common.querydsl.options.QuerydslNoOffsetNumberOptions;
import back.global.common.querydsl.options.QuerydslNoOffsetOptions;
import back.global.common.querydsl.options.QuerydslNoOffsetStringOptions;
import back.global.common.querydsl.reader.QuerydslNoOffsetPagingItemReader;
import back.member.application.domain.Member;
import back.member.application.domain.MemberStatus;
import back.member.application.domain.QMember;
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

import static back.member.application.domain.QMember.*;

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
