package com.prhythm.ssjv001.factor;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpHeaders;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Headers extends HttpHeaders {

    public static final String PX_TRACE_CODE = "PX-Trace-Code";
    public static final String PX_TRACE_MESSAGE = "PX-Trace-Message";
}
