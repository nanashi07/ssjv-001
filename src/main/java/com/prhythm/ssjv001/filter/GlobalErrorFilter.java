package com.prhythm.ssjv001.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prhythm.ssjv001.error.GenericException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static com.prhythm.ssjv001.factor.Headers.PX_TRACE_CODE;
import static com.prhythm.ssjv001.factor.Headers.PX_TRACE_MESSAGE;

@Slf4j
@Component
@RequiredArgsConstructor
public class GlobalErrorFilter implements WebFilter {

    private final ObjectMapper objectMapper;

    @Override
    @NonNull
    public Mono<Void> filter(@NonNull ServerWebExchange exchange, WebFilterChain chain) {
        return chain.filter(exchange)
                .onErrorResume(GenericException.class, error -> handleGenericException(exchange.getRequest(), exchange.getResponse(), error))
                .onErrorResume(Exception.class, error -> handleDefaultException(exchange.getRequest(), exchange.getResponse(), error));
    }

    private Mono<Void> handleDefaultException(ServerHttpRequest request, ServerHttpResponse response, Exception error) {
        log.error("default error handler,{} {}", request.getMethod(), request.getPath(), error);
        if (error instanceof ResponseStatusException rse) {
            response.setStatusCode(rse.getStatus());
        } else {
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        // response empty data
        DataBuffer buffer = response.bufferFactory().wrap("{}".getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    private Mono<Void> handleGenericException(ServerHttpRequest request, ServerHttpResponse response, GenericException error) {
        log.warn("generic error handler: {} {}, message = {}", request.getMethod(), request.getPath(), error.getMessage());
        HttpHeaders headers = response.getHeaders();

        // put trace code
        headers.set(PX_TRACE_CODE, error.getTraceCode());
        headers.set(PX_TRACE_MESSAGE, error.getMessage());

        Optional<HttpStatus> status = error.status();
        if (status.isPresent()) {
            response.setStatusCode(status.get());
        } else {
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        DataBuffer buffer = null;
        if (error.getData() != null) {
            try {
                buffer = response.bufferFactory()
                        .wrap(objectMapper.writeValueAsBytes(error.getData()));
            } catch (Exception ex) {
                log.error("handle failed", ex);
            }
        }

        return response.writeWith(Mono.just(Optional.ofNullable(buffer)
                .orElse(response.bufferFactory().wrap("{}".getBytes(StandardCharsets.UTF_8)))));
    }

}
