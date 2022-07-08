package com.prhythm.ssjv001.service;

import com.prhythm.ssjv001.message.vo.MessageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.MonoSink;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketResponseDispatcher {

    private final Map<String, MonoSink<MessageResponse>> consumers = new ConcurrentHashMap<>();

    public void register(String ticketId, MonoSink<MessageResponse> sink) {
        log.info("register for ticket: {}", ticketId);
        consumers.put(ticketId, sink);
    }

    public void unregister(String ticketId) {
        log.info("unregister for ticket: {}", ticketId);
        consumers.remove(ticketId);
    }

    public synchronized void dispatch(MessageResponse response) {
        if (consumers.containsKey(response.getTicketId())) {
            MonoSink<MessageResponse> sink = consumers.remove(response.getTicketId());
            log.info("dispatch response for ticket: {}", response.getTicketId());
            sink.success(response);
        } else {
            log.warn("expired ticket, cancel succeed ticket");
            // failed acknowledge
        }
    }

}
