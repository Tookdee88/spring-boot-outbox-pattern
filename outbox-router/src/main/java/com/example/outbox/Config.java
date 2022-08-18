package com.example.outbox;

import org.apache.kafka.common.config.ConfigDef;

public class Config {
    static final String TOPIC = "topic";

    public static ConfigDef configDef() {
        return new ConfigDef()
                .define(TOPIC,
                        ConfigDef.Type.STRING,
                        "outbox_events",
                        ConfigDef.Importance.LOW,
                        "The routed topic");
    }
}
