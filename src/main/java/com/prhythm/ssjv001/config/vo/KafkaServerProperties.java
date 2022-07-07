package com.prhythm.ssjv001.config.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class KafkaServerProperties {
    public static final String TOPIC_DIRECTIVE_TICKET = "prhythm_ssjv001_directive_ticket";
    public static final String TOPIC_DIRECTIVE_RESPONSE = "prhythm_ssjv001_directive_response";

    private boolean enabled;
    private String bootstrapAddress;
}
