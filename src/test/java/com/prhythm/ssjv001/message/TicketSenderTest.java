package com.prhythm.ssjv001.message;

import com.prhythm.ssjv001.service.vo.TicketInfo;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@SpringBootTest
class TicketSenderTest {

    @Resource
    private TicketSender ticketSender;

    @Test
    @SneakyThrows
    void testSendCommand() {
        var ticket = TicketInfo.builder()
                .command("test")
                .ticketId(UUID.randomUUID().toString())
                .build();
        ticketSender.sendTicket(ticket)
                .subscribe(result -> {
                    ProducerRecord<String, String> producerRecord = result.getProducerRecord();
                    RecordMetadata recordMetadata = result.getRecordMetadata();
                    log.info("producerRecord: {}", producerRecord);
                    log.info("recordMetadata: {}", recordMetadata);
                });

        TimeUnit.SECONDS.sleep(5);
        log.info("test done");
    }

}