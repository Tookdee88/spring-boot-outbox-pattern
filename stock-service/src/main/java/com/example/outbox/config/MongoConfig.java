package com.example.outbox.config;

import com.mongodb.ReadPreference;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.WriteResultChecking;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

@Configuration
public class MongoConfig {

    @Bean
    public ReactiveMongoTemplate reactiveMongoTemplate(ReactiveMongoDatabaseFactory factory, MongoConverter converter) {
        ReactiveMongoTemplate template = new ReactiveMongoTemplate(factory, converter);
        template.setReadPreference(ReadPreference.primaryPreferred());
        template.setWriteResultChecking(WriteResultChecking.EXCEPTION);
        return template;
    }

    @Bean
    public MongoCustomConversions customConversions() {
        List<Object> converters = new ArrayList<>();
        converters.add(new LocalDateTimeReadConverter());
        return new MongoCustomConversions(converters);
    }

    @ReadingConverter
    public class LocalDateTimeReadConverter implements Converter<Date, LocalDateTime> {
        @Override
        public LocalDateTime convert(Date date) {
            return date.toInstant().atZone(ZoneOffset.UTC).toLocalDateTime();
        }
    }
}
