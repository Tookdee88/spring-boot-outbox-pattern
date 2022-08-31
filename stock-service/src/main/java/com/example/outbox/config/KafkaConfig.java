package com.example.outbox.config;

import com.example.outbox.Event;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;

import java.time.Duration;
import java.util.*;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class KafkaConfig {
    private static final String CONFLUENT_CLOUD_DOMAIN = "confluent.cloud";

    @Value("${kafka.topics:order_outbox_events}")
    private List<String> topics;

    @Bean
    @ConfigurationProperties(prefix = "kafka")
    public KafkaProperties kafkaProperties() {
        return new KafkaProperties();
    }

    @Bean
    @ConfigurationProperties(prefix = "schema-registry")
    public SchemaRegistryProperties schemaRegistryProperties() {
        return new SchemaRegistryProperties();
    }

    @Bean
    public ReceiverOptions<String, Event> receiverOptions() {
        val kafkaProperties = kafkaProperties();
        val schemaRegistryProperties = schemaRegistryProperties();
        val props = Arrays.asList(
                kafkaProperties.asProperties(),
                schemaRegistryProperties.asProperties(),
                kafkaProperties.consumerProperties()
        )
                .stream()
                .collect(Properties::new, Map::putAll, Map::putAll);
        return ReceiverOptions.create(props);
    }

    @Bean
    public KafkaReceiver<String, Event> receiver(ReceiverOptions<String, Event> receiverOptions) {
        val groupId = receiverOptions.groupId();
        val clientId = receiverOptions.consumerProperty(ConsumerConfig.CLIENT_ID_CONFIG);
        return KafkaReceiver.create(receiverOptions.subscription(topics)
                .addAssignListener(partitions -> log.info("[{}] onPartitionsAssigned [{}] => [{}]", groupId, clientId, partitions))
                .addRevokeListener(partitions -> log.info("[{}] onPartitionsRevoked [{}] => [{}]", groupId, clientId, partitions))
                .closeTimeout(Duration.ofSeconds(60)));
    }

    @Data
    public static class KafkaProperties {
        // Kafka configs
        private static final String SECURITY_PROTOCOL = "SASL_SSL";
        private static final String SASL_MECHANISM = "PLAIN";
        private static final String SASL_JAAS_CONFIG = "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"%s\" password=\"%s\";";
        private static final String SSL_ENDPOINT_IDENTIFICATION_ALGORITHM = "https";
        private static final String CLIENT_DNS_LOOKUP = "use_all_dns_ips";
        private String bootstrapServers;
        private String apiKey;
        private String apiSecret;

        // Consumer configs
        private static final Boolean SPECIFIC_AVRO_READER_CONFIG = Boolean.TRUE;
        private String groupId;
        private String autoOffsetReset;
        private Integer retryBackoffMs;
        private Integer reconnectBackoffMs;
        private Integer reconnectBackoffMaxMs;
        private Integer requestTimeoutMs;
        private Integer heartbeatIntervalMs;
        private Integer sessionTimeoutMs;
        private Integer maxPollIntervalMs;
        private Integer maxPollRecords;

        // Producer configs
        private static final String ACKS_CONFIG = "all";
        private static final Boolean AUTO_REGISTER_SCHEMAS = Boolean.TRUE;

        public Properties asProperties() {
            val props = new Properties();
            props.put("bootstrap.servers", bootstrapServers);
            if (bootstrapServers.contains(CONFLUENT_CLOUD_DOMAIN)) {
                props.put("security.protocol", SECURITY_PROTOCOL);
                props.put("sasl.mechanism", SASL_MECHANISM);
                props.put("sasl.jaas.config", String.format(SASL_JAAS_CONFIG, apiKey, apiSecret));
                props.put("ssl.endpoint.identification.algorithm", SSL_ENDPOINT_IDENTIFICATION_ALGORITHM);

                // Recommended JVM settings by Confluent, https://docs.confluent.io/current/cloud/faq.html#what-are-the-recommended-jvm-settings
                props.put("client.dns.lookup", CLIENT_DNS_LOOKUP);
            }
            return props;
        }

        public Properties consumerProperties() {
            val props = new Properties();
            props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
            props.put(ConsumerConfig.CLIENT_ID_CONFIG, String.format("%s-%s", groupId, UUID.randomUUID()));
            props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
            props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);
            props.put(ConsumerConfig.RECONNECT_BACKOFF_MS_CONFIG, reconnectBackoffMs);
            props.put(ConsumerConfig.RECONNECT_BACKOFF_MAX_MS_CONFIG, reconnectBackoffMaxMs);
            props.put(ConsumerConfig.RETRY_BACKOFF_MS_CONFIG, retryBackoffMs);
            props.put(ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG, requestTimeoutMs);
            props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, heartbeatIntervalMs);
            props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, sessionTimeoutMs);
            props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, maxPollIntervalMs);
            props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxPollRecords);
            props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
            props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
            props.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, KafkaAvroDeserializer.class);
            props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, KafkaAvroDeserializer.class);
            props.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, SPECIFIC_AVRO_READER_CONFIG);
            return props;
        }
    }

    @Data
    public static class SchemaRegistryProperties {
        private static final String BASIC_AUTH_CREDENTIALS_SOURCE = "USER_INFO";

        private String url;
        private String apiKey;
        private String apiSecret;

        public Properties asProperties() {
            val props = new Properties();
            props.put("schema.registry.url", url);
            if (url.contains(CONFLUENT_CLOUD_DOMAIN)) {
                props.put("basic.auth.credentials.source", BASIC_AUTH_CREDENTIALS_SOURCE);
                props.put("basic.auth.user.info", String.format("%s:%s", apiKey, apiSecret));
            }
            return props;
        }
    }
}