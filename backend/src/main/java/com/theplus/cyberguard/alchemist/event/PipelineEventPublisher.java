package com.theplus.cyberguard.alchemist.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
@Slf4j
public class PipelineEventPublisher {

    private final SimpMessagingTemplate websocket;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public PipelineEventPublisher(
            SimpMessagingTemplate websocket,
            @Autowired(required = false) KafkaTemplate<String, Object> kafkaTemplate) {
        this.websocket = websocket;
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishStepUpdate(UUID projectId, String step, String status, Map<String, Object> details) {
        Map<String, Object> event = Map.of(
                "projectId", projectId.toString(),
                "step", step,
                "status", status,
                "details", details,
                "timestamp", System.currentTimeMillis()
        );

        websocket.convertAndSend("/topic/pipeline/" + projectId, event);

        if (kafkaTemplate != null) {
            try {
                kafkaTemplate.send("alchemist.pipeline.events", projectId.toString(), event);
            } catch (Exception e) {
                log.warn("Kafka publish failed (non-critical): {}", e.getMessage());
            }
        }

        log.debug("Pipeline event: project={}, step={}, status={}", projectId, step, status);
    }
}
