package back.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.List;

@SpringBatchTest
@SpringJUnitConfig
public class BatchTestSupport {

    @Autowired
    protected JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    protected EntityManagerFactory entityManagerFactory;

    @Autowired
    protected DataSource dataSource;

    protected EntityManager entityManager;

    protected JdbcTemplate jdbcTemplate;

    @BeforeEach
    public void setUp() {
        entityManager = entityManagerFactory.createEntityManager();
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    /**
     * Get a JobParametersBuilder with unique values.
     * @return JobParametersBuilder
     */
    protected JobParametersBuilder getUniqueParameterBuilder() {
        return jobLauncherTestUtils.getUniqueJobParametersBuilder();
    }

    /**
     * Launch the job.
     * @return JobExecution
     */
    protected JobExecution launchJob() throws Exception {
        return jobLauncherTestUtils.launchJob();
    }

    /**
     * Launch the job with parameters.
     * @param jobParameters JobParameters
     * @return JobExecution
     */
    protected JobExecution launchJob(JobParameters jobParameters) throws Exception {
        return jobLauncherTestUtils.launchJob(jobParameters);
    }

    /**
     * Save an entity.
     * @param entity Object
     * @return Object
     */
    protected Object save(Object entity) {
        entityManager.persist(entity);
        return entity;
    }

    /**
     * Save a list of entities.
     * @param entities List<Object>
     * @return List<Object>
     */
    protected List<Object> saveAll(List<Object> entities) {
        entities.forEach(entityManager::persist);
        return entities;
    }

    /**
     * Delete an entity.
     * @param entity Object
     * @return Object
     */
    protected Object delete(Object entity) {
        entityManager.remove(entity);
        return entity;
    }

    /**
     * Delete a list of entities.
     * @param entities List<Object>
     * @return List<Object>
     */
    protected List<Object> deleteAll(List<Object> entities) {
        entities.forEach(entityManager::remove);
        return entities;
    }

    @AfterEach
    public void tearDown() {
        if (entityManager != null) {
            entityManager.close();
        }
        if (jdbcTemplate != null) {
            // jdbcTemplate.update("DELETE FROM <your_table_name>");
        }
    }



}
