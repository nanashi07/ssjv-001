package com.prhythm.ssjv001.controller;

import com.prhythm.ssjv001.controller.vo.CreateTicketRequest;
import com.prhythm.ssjv001.controller.vo.TicketCreatedResponse;
import com.prhythm.ssjv001.error.ExceptionBuilder;
import com.prhythm.ssjv001.service.TicketService;
import com.prhythm.ssjv001.service.vo.TicketResult;
import com.prhythm.ssjv001.trace.TraceCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;

@Slf4j
@RestController
@RequestMapping("/ticket")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @PostMapping("/create")
    public Mono<TicketCreatedResponse> create(@RequestBody CreateTicketRequest request) {
        return Mono.just(request)
                .flatMap(ticketService::validate)  // pre-handler
                .flatMap(ticketService::sendTicket)  // external async process
                .flatMap(t -> t.getT2().zipWith(ticketService.persistTicket(t.getT1())))
                .flatMap(t -> ticketService.handleResponse(t.getT2(), t.getT1()))
                .map(this::convert)
                .timeout(Duration.ofSeconds(5), timeout(request))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @NonNull
    private TicketCreatedResponse convert(@NonNull TicketResult result) {
        return TicketCreatedResponse.builder()
                .ticketId(result.getTicketId())
                .message(result.getMessage())
                .command(result.getCommand())
                .type(result.getType())
                .created(result.getCreated())
                .build();
    }

    @NonNull
    private Mono<TicketCreatedResponse> timeout(CreateTicketRequest request) {
        return Mono.defer(() -> {
            log.warn("failed for {}", request);
            // cancel mapping for response here
            var response = TicketCreatedResponse.builder()
                    .message("Ticket created failed")
                    .command(request.getCommand())
                    .type(request.getType())
                    .build();

            return ExceptionBuilder.timeout("create ticket timeout internally", TraceCode.Ticket.EXCEED_TIMEOUT, response).toMono();
        });
    }

}
