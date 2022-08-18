FROM confluentinc/cp-kafka-connect:7.1.1

COPY outbox-router/target/outbox-router-0.0.1-SNAPSHOT.jar /etc/kafka-connect/jars/

RUN confluent-hub install --no-prompt debezium/debezium-connector-mongodb:1.9.2 \
    && confluent-hub install --no-prompt mongodb/kafka-connect-mongodb:1.7.0
