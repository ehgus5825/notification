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
                .<String, Notification>chunk(chunkSize)
                .reader(updateIngredientReader(null))
                .processor(updateIngredientProcessor(null,null))
                .writer(updateIngredientWriter())
                .build();
    }

    @Bean
    @StepScope
    public JpaPagingItemReader<String> updateIngredientReader(@Value("#{jobParameters['name']}") String name) {

        Map<String, Object> parameterValue = new HashMap<>();
        parameterValue.put("name", name);

        return new JpaPagingItemReaderBuilder<String>()
                .name("updateIngredientReader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(chunkSize)
                .queryString("select si.email from SuggestedIngredient si where si.name = :name group by si.email")
                .parameterValues(parameterValue)
                .build();
    }

    @Bean
    @StepScope
    public ItemProcessor<String, Notification> updateIngredientProcessor(@Value("#{jobParameters['name']}") String name,
                                                                         @Value("#{jobParameters['id']}") Long id) {

        return email -> {
            adapter.update(email);

            Notification notification = Notification.create(
                    NotificationType.INGREDIENT,
                    "/api/ingredients/unit/" + id,
                    email,
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
