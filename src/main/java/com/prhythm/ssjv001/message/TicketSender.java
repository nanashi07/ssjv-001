package com.prhythm.ssjv001.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prhythm.ssjv001.config.vo.KafkaServerProperties;
import com.prhythm.ssjv001.service.vo.TicketInfo;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Component
@RequiredArgsConstructor
public class TicketSender {

    private final KafkaServerProperties config;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @SneakyThrows
    public Mono<SendResult<String, String>> sendTicket(TicketInfo ticket) {
        return sendCommand(objectMapper.writeValueAsString(ticket));
    }

    private Mono<SendResult<String, String>> sendCommand(String command) {
        if (!config.isEnabled()) {
            log.warn("kafka message is not enabled");
            return Mono.empty();
        }
        return Mono
                .fromFuture(
                        kafkaTemplate
                                .send(KafkaServerProperties.TOPIC_DIRECTIVE_TICKET, command)
                                .completable()
                )
                .retry(2)
                .subscribeOn(Schedulers.boundedElastic());
    }

}
