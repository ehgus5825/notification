package back.global.batch;

import back.global.common.querydsl.expression.Expression;
import back.global.common.querydsl.options.QuerydslNoOffsetNumberOptions;
import back.global.common.querydsl.reader.QuerydslNoOffsetPagingItemReader;
import back.ingredient.application.domain.QSuggestedIngredient;
import back.ingredient.application.domain.SuggestedIngredient;
import back.member.application.domain.Member;
import back.member.application.domain.MemberStatus;
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
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

import javax.persistence.EntityManagerFactory;
import java.util.HashMap;
import java.util.Map;

import static back.ingredient.application.domain.QSuggestedIngredient.*;
import static back.member.application.domain.QMember.member;

@RequiredArgsConstructor
@Configuration
@Slf4j
public class NotificationAddIngredientConfig {

    private final EntityManagerFactory entityManagerFactory;
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    private final NotificationByMemberAdapter adapter;

    @Value("${chunkSize:1000}")
    private int chunkSize = 1000;


    @Bean
    public Job updateIngredientJob() {
        return jobBuilderFactory.get("updateIngredientJob")
                .preventRestart()
                .start(updateIngredientStep())
                .build();
    }

    @Bean
    @JobScope
    public Step updateIngredientStep() {
        return stepBuilderFactory.get("updateIngredientStep")
                .<SuggestedIngredient, Notification>chunk(chunkSize)
                .reader(updateIngredientReader(null))
                .processor(updateIngredientProcessor(null,null))
                .writer(updateIngredientWriter())
                .build();
    }

    @Bean
    @StepScope
    public QuerydslNoOffsetPagingItemReader<SuggestedIngredient> updateIngredientReader(@Value("#{jobParameters['name']}") String name) {

        QuerydslNoOffsetNumberOptions<SuggestedIngredient, Long> option = new QuerydslNoOffsetNumberOptions<>(suggestedIngredient.id, Expression.ASC);

        return new QuerydslNoOffsetPagingItemReader<>(entityManagerFactory, chunkSize, option,
                queryFactory -> queryFactory
                        .selectFrom(suggestedIngredient)
                        .where(
                                suggestedIngredient.name.eq(name),
                                member.memberStatus.eq(MemberStatus.STEADY_STATUS))
                        .leftJoin(member).on(member.email.eq(suggestedIngredient.email)));
    }

    @Bean
    @StepScope
    public ItemProcessor<SuggestedIngredient, Notification> updateIngredientProcessor(@Value("#{jobParameters['name']}") String name,
                                                                         @Value("#{jobParameters['id']}") Long id) {

        return suggestedIngredient -> {
            adapter.update(suggestedIngredient.getEmail());

            Notification notification = Notification.create(
                    NotificationType.INGREDIENT,
                    "/refrigerator/add/info?ingredient" + name,
                    suggestedIngredient.getEmail(),
                    HttpMethod.GET.name());
            notification.createIngredientMessage(name);
            return notification;
        };
    }

    @Bean
    @StepScope
    public JpaItemWriter<Notification> updateIngredientWriter() {
        JpaItemWriter<Notification> jpaItemWriter = new JpaItemWriter<>();
        jpaItemWriter.setEntityManagerFactory(entityManagerFactory);
        return jpaItemWriter;
    }
}
