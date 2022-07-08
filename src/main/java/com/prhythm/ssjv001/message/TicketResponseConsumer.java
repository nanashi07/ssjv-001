package com.prhythm.ssjv001.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prhythm.ssjv001.message.vo.MessageResponse;
import com.prhythm.ssjv001.service.TicketResponseDispatcher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import static com.prhythm.ssjv001.config.vo.KafkaServerProperties.TOPIC_DIRECTIVE_RESPONSE;

@Slf4j
@Component
@RequiredArgsConstructor
public class TicketResponseConsumer {

    private final ObjectMapper objectMapper;
    private final TicketResponseDispatcher ticketResponseDispatcher;

    @KafkaListener(topics = {TOPIC_DIRECTIVE_RESPONSE}, groupId = "ssjv001")
    public void resolverHandler(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) Integer key,
                                @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition,
                                @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                @Header(KafkaHeaders.RECEIVED_TIMESTAMP) long ts,
                                @Payload String message) {
        log.info("receive ticket response: {}, key: {}, partition: {}, topic: {}, ts: {}", message, key, partition, topic, ts);
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
