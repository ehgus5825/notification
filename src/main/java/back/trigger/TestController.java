package back.trigger;

import back.batch.NotificationAddIngredientConfig;
import back.batch.NotificationAddNoticeConfig;
import back.batch.NotificationScheduleConfig;
import back.exception.BasicException;
import back.entity.RegisteredIngredient;
import back.entity.Notice;
import back.exception.NotificationExceptionType;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class TestController {

    private final JobLauncher jobLauncher;
    private final ApplicationContext context;
    private final EntityManager em;

    @GetMapping("/notice/run/{id}")
    public ExitStatus noticeJobRun(@PathVariable("id") Long id) throws Exception {

        Notice notice = em.createQuery("select n from Notice n where n.id = :id", Notice.class)
                .setParameter("id", id)
                .getResultList()
                .stream()
                .findAny()
                .orElseThrow(() -> new BasicException(NotificationExceptionType.NOTICE_NOTIFICATION_CREATE_FAIL));

        NotificationAddNoticeConfig noticeConfig = context.getBean(NotificationAddNoticeConfig.class);

        Map<String, JobParameter> jobParametersMap = new HashMap<>();

        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:SSS");

        jobParametersMap.put("nowDate", new JobParameter(LocalDateTime.now().format(format)));
        jobParametersMap.put("id", new JobParameter(id));
        jobParametersMap.put("title", new JobParameter(notice.getTitle()));

        JobParameters parameters = new JobParameters(jobParametersMap);

        return jobLauncher.run(noticeConfig.noticeJob(), parameters).getExitStatus();
    }

    @GetMapping("/ingredient/update/run/{id}")
    public ExitStatus addIngredientJobRun(@PathVariable("id") Long id) throws Exception {

        RegisteredIngredient ingredient = em.createQuery("select ri from RegisteredIngredient ri where ri.id = :id", RegisteredIngredient.class)
                .setParameter("id", id)
                .getResultList()
                .stream()
                .findAny()
                .orElseThrow(() -> new BasicException(NotificationExceptionType.ADD_INGREDIENT_NOTIFICATION_CREATE_FAIL));

        NotificationAddIngredientConfig ingredientConfig = context.getBean(NotificationAddIngredientConfig.class);

        Map<String, JobParameter> jobParametersMap = new HashMap<>();

        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:SSS");

        jobParametersMap.put("nowDate", new JobParameter(LocalDateTime.now().format(format)));
        jobParametersMap.put("id", new JobParameter(id));
        jobParametersMap.put("name", new JobParameter(ingredient.getName()));

        JobParameters parameters = new JobParameters(jobParametersMap);

        return jobLauncher.run(ingredientConfig.updateIngredientJob(), parameters).getExitStatus();
    }

    @GetMapping("/scheduled/run/")
    public ExitStatus scheduledJobRun() throws Exception {

        NotificationScheduleConfig scheduleConfig = context.getBean(NotificationScheduleConfig.class);

        Map<String, JobParameter> jobParametersMap = new HashMap<>();

        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:SSS");

        jobParametersMap.put("date", new JobParameter(LocalDateTime.now().format(format)));

        JobParameters parameters = new JobParameters(jobParametersMap);

        return jobLauncher.run(scheduleConfig.scheduleJob(), parameters).getExitStatus();
    }
}
