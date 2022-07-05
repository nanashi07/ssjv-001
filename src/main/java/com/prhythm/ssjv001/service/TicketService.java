package com.prhythm.ssjv001.service;

import com.prhythm.ssjv001.controller.vo.CreateTicketRequest;
import com.prhythm.ssjv001.message.TicketSender;
import com.prhythm.ssjv001.message.vo.MessageResponse;
import com.prhythm.ssjv001.service.vo.TicketResult;
import com.prhythm.ssjv001.service.vo.TicketInfo;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.Date;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class TicketService {

    private final TicketSender ticketSender;
    private final TicketResponseDispatcher ticketResponseDispatcher;

    public Mono<TicketInfo> validate(CreateTicketRequest request) {
        log.info("validate request: {}", request);
        return Mono.just(request)
                .filter(r -> StringUtils.hasText(request.getType())
                        && StringUtils.hasText(request.getCommand())
                        && StringUtils.hasText(request.getSubmitter()))
                .map(r -> TicketInfo.builder()
                        .command(r.getCommand())
                        .type(r.getType())
                        .submitter(r.getSubmitter())
                        .build())
                .switchIfEmpty(Mono.error(IllegalArgumentException::new));
    }

    public Mono<Tuple2<TicketInfo, Mono<MessageResponse>>> sendTicket(TicketInfo source) {
        log.info("send ticket: {}", source);
        var ticket = source.toBuilder()
                .ticketId(UUID.randomUUID().toString())
                .status("INIT")
                .build();

        // create binding
        var binding = Mono.<MessageResponse>create(sink -> {
            // bind to consumer
            ticketResponseDispatcher.register(ticket.getTicketId(), sink);
        });
        return ticketSender.sendTicket(ticket)
                .then(Mono.just(Tuples.of(ticket, binding)));
    }

    @SneakyThrows
    public Mono<TicketInfo> persistTicket(TicketInfo ticket) {
        log.info("persist ticket: {}", ticket);
        ticket.setCreateTime(new Date());
        // do persist here
        return Mono.just(ticket);
    }

    public Mono<TicketResult> handleResponse(TicketInfo ticket, MessageResponse response) {
        log.info("handler response, ticket: {}, response: {}", ticket, response);
        // handler response here
        var result = TicketResult.builder()
                .ticketId(ticket.getTicketId())
                .type(ticket.getType())
                .command(ticket.getCommand())
                .message("Ticket accepted")
                .created(ticket.getCreateTime())
                .build();
        return Mono.just(result);
    }
}
