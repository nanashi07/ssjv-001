package com.prhythm.ssjv001.config.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class KafkaServerProperties {
    public static final String TOPIC_SIMPLE_COMMAND = "prhythm_ssjv001_simple_command";
    public static final String TOPIC_SIMPLE_RESOLVER = "prhythm_ssjv001_simple_resolver";

    private boolean enabled;
    private String bootstrapAddress;
}
