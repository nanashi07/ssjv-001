package com.prhythm.ssjv001.error;

import com.prhythm.ssjv001.trace.CodePack;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ExceptionBuilder {

    public static TimeoutException timeout(String message, CodePack traceCode, Object data) {
        TimeoutException exception = new TimeoutException(message);
        exception.setTraceCode(traceCode.code());
        exception.setData(data);
        return exception;
    }

    public static InvalidException invalid(String message, CodePack traceCode) {
        InvalidException exception = new InvalidException(message);
        exception.setTraceCode(traceCode.code());
        return exception;
    }

}
