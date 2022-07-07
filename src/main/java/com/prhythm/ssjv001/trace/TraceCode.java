package com.prhythm.ssjv001.trace;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TraceCode {

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public enum Ticket implements CodePack {

        INVALID("001"),
        EXCEED_TIMEOUT("002"),
        ;

        private static String TICKET_CODE = "T01";

        private final String code;

        @Override
        public String code() {
            return String.format("%s-%s", TICKET_CODE, code);
        }
    }

}
