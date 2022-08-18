package com.example.outbox;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.connect.connector.ConnectRecord;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.header.Headers;
import org.apache.kafka.connect.transforms.Transformation;
import org.apache.kafka.connect.transforms.util.SimpleConfig;

import java.util.Map;

@Slf4j
public class Router<R extends ConnectRecord<R>> implements Transformation<R> {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private String topic;

    @Override
    public void configure(Map<String, ?> configMap) {
        final SimpleConfig config = new SimpleConfig(Config.configDef(), configMap);
        topic = config.getString(Config.TOPIC);
    }

    @Override
    public R apply(R record) {
        // Ignoring tombstones just in case
        if (record.value() == null) {
            return record;
        }

        Struct struct = (Struct) record.value();
        String op = struct.getString("op");

        if (op.equals("c")) {
            try {
                OutboxEvent event = MAPPER.readValue(struct.getString("after"), OutboxEvent.class);

                String topic = String.format("%s_outbox_events", event.getAggregateType());
                String eventId = event.getEventId();
                String eventType = event.getEventType();
                String payload = event.getPayload();
                Long timestamp = struct.getInt64("ts_ms");

                Schema valueSchema = SchemaBuilder.struct()
                        .name("com.example.outbox.Event")
                        .field("eventType", Schema.STRING_SCHEMA)
                        .field("timestamp", Schema.INT64_SCHEMA)
                        .field("payload", Schema.STRING_SCHEMA)
                        .build();

                Struct value = new Struct(valueSchema)
                        .put("eventType", eventType)
                        .put("timestamp", timestamp)
                        .put("payload", payload);

                Headers headers = record.headers();
                headers.addString("eventId", eventId);

                log.info("[{}] [{}] Route event to topic [{}] => payload: {}", eventId, eventType, topic, payload);
                return record.newRecord(topic, null, Schema.STRING_SCHEMA, eventId, valueSchema, value,
                        record.timestamp(), headers);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
        return null;
    }

    @Override
    public ConfigDef config() {
        return Config.configDef();
    }

    @Override
    public void close() {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    public static class OutboxEvent {
        private String eventId;
        private String eventType;
        private String aggregateType;
        private String payload;
    }
}