package back.batch;

import back.repository.CommentRepository;
import back.repository.IngredientRepository;
import back.repository.MyBookmarkRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@RequiredArgsConstructor
@Configuration
@Slf4j
public class PeriodicDataDeleteConfig {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final CommentRepository commentRepository;
    private final IngredientRepository ingredientRepository;
    private final MyBookmarkRepository myBookmarkRepository;

    @Bean
    public Job periodicDeleteScheduleJob() {
        return jobBuilderFactory.get("periodicDeleteScheduleJob")
                .preventRestart()
                .start(deleteCommentStep())
                .next(deleteIngredientStep())
                .next(deleteBookmarkStep())
                .build();
    }

    @Bean
    @JobScope
    public Step deleteCommentStep() {
        return stepBuilderFactory.get("deleteCommentStep")
                .tasklet((contribution, chunkContext) -> {
                    commentRepository.deleteComment();
//                    commentRepository.deleteCommentHeart();
//                    commentRepository.deleteCommentHeartPeople();

                    return RepeatStatus.FINISHED;
                })
                .build();
    }

    @Bean
    @JobScope
    public Step deleteIngredientStep() {
        return stepBuilderFactory.get("deleteIngredientStep")
                .tasklet((contribution, chunkContext) -> {
                    ingredientRepository.deleteIngredient();

                    return RepeatStatus.FINISHED;
                })
                .build();
    }

    @Bean
    @JobScope
    public Step deleteBookmarkStep() {
        return stepBuilderFactory.get("deleteBookmarkStep")
                .tasklet((contribution, chunkContext) -> {
                    myBookmarkRepository.deleteMyBookmark();

                    return RepeatStatus.FINISHED;
                })
                .build();
    }
}
