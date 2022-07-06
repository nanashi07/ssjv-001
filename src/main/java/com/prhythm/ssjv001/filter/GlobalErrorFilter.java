package com.prhythm.ssjv001.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prhythm.ssjv001.error.GenericException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class GlobalErrorFilter implements WebFilter {

    private final ObjectMapper objectMapper;

    @Override
    @NonNull
    public Mono<Void> filter(@NonNull ServerWebExchange exchange, WebFilterChain chain) {
        return chain.filter(exchange)
                .onErrorResume(GenericException.class, error -> handleGenericException(exchange.getResponse(), error));
    }

    private Mono<Void> handleGenericException(ServerHttpResponse response, GenericException error) {
        HttpHeaders headers = response.getHeaders();
        headers.set("PX-Trace-Code", error.getTraceCode());
        headers.set("PX-Trace-Message", error.getMessage());

        Optional<HttpStatus> status = error.status();
        if (status.isPresent()) {
            response.setStatusCode(status.get());
        } else {
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        if (error.getData() != null) {
            try {
                DataBuffer buffer = response.bufferFactory()
                        .wrap(objectMapper.writeValueAsBytes(error.getData()));
                return response.writeWith(Mono.just(buffer));
            } catch (Exception ex) {
                log.error("handle failed", ex);
            }
        }

        return error.toMono();
    }

}
