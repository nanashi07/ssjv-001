package com.prhythm.ssjv001.error;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import reactor.core.publisher.Mono;

import java.util.Optional;

public class GenericException extends RuntimeException {

    @Getter
    @Setter
    private String traceCode;
    @Getter
    @Setter
    private Object data;

    public GenericException() {
        super();
    }

    public GenericException(String message) {
        super(message);
    }

    public GenericException(String message, Throwable cause) {
        super(message, cause);
    }

    public GenericException(Throwable cause) {
        super(cause);
    }

    protected GenericException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public <T> Mono<T> toMono() {
        return Mono.error(this);
    }

    public Optional<HttpStatus> status() {
        return Optional.ofNullable(getClass().getDeclaredAnnotation(ResponseStatus.class))
                .map(ResponseStatus::value);
    }
}
