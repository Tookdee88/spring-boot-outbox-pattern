package com.example.outbox.config;

import com.example.outbox.model.StringEnum;
import com.mongodb.ReadPreference;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.GenericConverter;
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
        converters.add(new StringEnumConverter());
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

    public class StringEnumConverter implements GenericConverter {
        @Override
        public Set<ConvertiblePair> getConvertibleTypes() {
            return new HashSet<>(Arrays.asList(
                    new ConvertiblePair(String.class, StringEnum.class),
                    new ConvertiblePair(StringEnum.class, String.class)
            ));
        }

        @Override
        public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
            if (sourceType.getType() == String.class) {
                for (StringEnum e : (StringEnum[]) targetType.getType().getEnumConstants()) {
                    if (source.equals(e.value())) {
                        return e;
                    }
                }
                throw new IllegalArgumentException("Invalid enum value " + source);
            } else if (StringEnum.class.isAssignableFrom(sourceType.getType())) {
                if (source == null) {
                    throw new IllegalArgumentException("enum value must not null");
                }
                return ((StringEnum) source).value();
            } else {
                throw new UnsupportedOperationException(sourceType.getName() + "is not supported");
            }
        }
    }
}
