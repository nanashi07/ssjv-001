package com.prhythm.ssjv001.external;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prhythm.ssjv001.config.vo.KafkaServerProperties;
import com.prhythm.ssjv001.message.vo.MessageResponse;
import com.prhythm.ssjv001.service.vo.TicketInfo;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class SimulatedExternalService {

    private final KafkaServerProperties config;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = {KafkaServerProperties.TOPIC_DIRECTIVE_TICKET}, groupId = "ssjv001")
    public void commandHandler(String message) {
        log.info("receive command: {}", message);
        try {
            var ticket = objectMapper.readValue(message, TicketInfo.class);
            log.info("handle command: {}, id: {}", ticket.getCommand(), ticket.getTicketId());
            // simulate time consuming process
            TimeUnit.MILLISECONDS.sleep(3000L + new SecureRandom().nextLong(3500L));
            var response = MessageResponse.builder()
                    .ticketId(ticket.getTicketId())
                    .status("ACCEPTED")
                    .build();
            sendResponse(response).subscribe();
        } catch (Exception e) {
            log.error("handler command error, message: {}", message, e);
            // handle retry
        }
    }

    @SneakyThrows
    public Mono<SendResult<String, String>> sendResponse(MessageResponse response) {
        return sendResult(objectMapper.writeValueAsString(response));
    }

    private Mono<SendResult<String, String>> sendResult(String result) {
        if (!config.isEnabled()) {
            log.warn("kafka message is not enabled");
            return Mono.empty();
        }
        log.info("send result: {}", result);
        return Mono
                .fromFuture(
                        kafkaTemplate
                                .send(KafkaServerProperties.TOPIC_DIRECTIVE_RESPONSE, result)
                                .completable()
                )
                .retry(2)
                .subscribeOn(Schedulers.boundedElastic());
    }

}
