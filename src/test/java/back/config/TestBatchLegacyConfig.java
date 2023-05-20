package back.config;

import back.global.config.QuerydslConfig;
import back.notification.adapter.out.persistence.NotificationByMemberAdapter;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableAutoConfiguration
@EnableBatchProcessing
@EnableConfigurationProperties
@EnableJpaAuditing
@EntityScan("back")
@EnableJpaRepositories("back.notification.adapter.out.repository")
@EnableTransactionManagement
@Import(QuerydslConfig.class)
public class TestBatchLegacyConfig { }

