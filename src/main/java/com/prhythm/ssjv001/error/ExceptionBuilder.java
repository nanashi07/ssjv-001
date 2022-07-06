package com.prhythm.ssjv001.error;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ExceptionBuilder {

    public static TimeoutException timeout(String message, String traceCode, Object data) {
        TimeoutException exception = new TimeoutException(message);
        exception.setTraceCode(traceCode);
        exception.setData(data);
        return exception;
    }

}
