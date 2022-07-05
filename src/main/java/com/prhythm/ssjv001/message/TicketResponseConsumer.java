package com.prhythm.ssjv001.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prhythm.ssjv001.config.vo.KafkaServerProperties;
import com.prhythm.ssjv001.message.vo.MessageResponse;
import com.prhythm.ssjv001.service.TicketResponseDispatcher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TicketResponseConsumer {

    private final ObjectMapper objectMapper;
    private final TicketResponseDispatcher ticketResponseDispatcher;

    @KafkaListener(topics = {KafkaServerProperties.TOPIC_SIMPLE_RESOLVER}, groupId = "ssjv001")
    public void resolverHandler(String message) {
        log.info("receive ticket response: {}", message);
        try {
            var response = objectMapper.readValue(message, MessageResponse.class);
            log.info("handle response: {}, status: {}", response.getTicketId(), response.getStatus());
            // dispatch to source request
            ticketResponseDispatcher.dispatch(response);
        } catch (Exception e) {
            log.error("handler response error, message: {}", message, e);
            // handle retry
        }
    }

}
