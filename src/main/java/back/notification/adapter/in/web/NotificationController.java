package back.notification.adapter.in.web;

import back.global.batch.NotificationAddIngredientConfig;
import back.global.batch.NotificationAddNoticeConfig;
import back.notification.adapter.in.dto.UpdateIngredientRequestDTO;
import back.notification.adapter.in.dto.AddNoticeRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class NotificationController {

    private final JobLauncher jobLauncher;
    private final ApplicationContext context;

    @PostMapping("/notice/run")
    public ExitStatus noticeJobRun(@RequestBody AddNoticeRequestDTO request) throws Exception {
        NotificationAddNoticeConfig noticeConfig = context.getBean(NotificationAddNoticeConfig.class);

        Map<String, JobParameter> jobParametersMap = new HashMap<>();

        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:SSS");

        jobParametersMap.put("nowDate", new JobParameter(LocalDateTime.now().format(format)));
        jobParametersMap.put("id", new JobParameter(request.getId()));
        jobParametersMap.put("title", new JobParameter(request.getTitle()));

        JobParameters parameters = new JobParameters(jobParametersMap);

        return jobLauncher.run(noticeConfig.noticeJob(), parameters).getExitStatus();
    }

    @PostMapping("/ingredient/update/run")
    public ExitStatus addIngredientJobRun(@RequestBody UpdateIngredientRequestDTO request) throws Exception {
        NotificationAddIngredientConfig ingredientConfig = context.getBean(NotificationAddIngredientConfig.class);

        Map<String, JobParameter> jobParametersMap = new HashMap<>();

        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:SSS");

        jobParametersMap.put("nowDate", new JobParameter(LocalDateTime.now().format(format)));
        jobParametersMap.put("id", new JobParameter(request.getId()));
        jobParametersMap.put("name", new JobParameter(request.getName()));

        JobParameters parameters = new JobParameters(jobParametersMap);

        return jobLauncher.run(ingredientConfig.updateIngredientJob(), parameters).getExitStatus();
    }
}
