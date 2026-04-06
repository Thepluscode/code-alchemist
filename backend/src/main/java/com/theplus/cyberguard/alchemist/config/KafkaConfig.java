package com.theplus.cyberguard.alchemist.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "spring.kafka.bootstrap-servers", matchIfMissing = false)
public class KafkaConfig {

    @Bean
    public NewTopic pipelineEventsTopic() {
        return new NewTopic("alchemist.pipeline.events", 3, (short) 1);
    }

    @Bean
    public NewTopic auditEventsTopic() {
        return new NewTopic("alchemist.audit.events", 3, (short) 1);
    }
}
