package ru.practicum.kafka;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.VoidDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import ru.practicum.deserializer.EventsSimilarityAvroDeserializer;
import ru.practicum.deserializer.UserActionAvroDeserializer;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.properties.CustomProperties;

import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class KafkaConsumerConfig {

    private final CustomProperties customProperties;

    // общие свойства потребителя Kafka
    private Map<String, Object> getCommonConsumerProps() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, VoidDeserializer.class);
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, customProperties.getKafka().getBootstrapServers());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, customProperties.getKafka().getAutoOffsetReset());
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, customProperties.getKafka().getEnableAutoCommit());
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, customProperties.getKafka().getMaxPollRecords());
        return props;
    }

    // Создает фабрику потребителей для сообщений UserActionAvro.
    @Bean
    public ConsumerFactory<String, UserActionAvro> userActionConsumerFactory() {
        Map<String, Object> props = getCommonConsumerProps(); // получаем общие настройки
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, UserActionAvroDeserializer.class);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, customProperties.getKafka().getUserActionConsumerGroup()); // группа потребления
        return new DefaultKafkaConsumerFactory<>(props);
    }

    // Создает фабрику потребителей для сообщений EventSimilarityAvro.
    @Bean
    public ConsumerFactory<String, EventSimilarityAvro> eventsSimilarityConsumerFactory() {
        Map<String, Object> props = getCommonConsumerProps(); // общие настройки
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, EventsSimilarityAvroDeserializer.class);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, customProperties.getKafka().getEventsSimilarityConsumerGroup()); // отдельная группа
        return new DefaultKafkaConsumerFactory<>(props);
    }

    // Заводит контейнер фабрики слушателей для обработки сообщений UserActionAvro.
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, UserActionAvro> userActionListenerContainerFactory() {
        var factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(userActionConsumerFactory()); // задаём фабрику потребителя
        factory.setAutoStartup(false); // слушатели запускаются вручную
        factory.setBatchListener(false); // обработка одиночных сообщений
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD); // подтверждение записи
        return factory;
    }

    // Заводит контейнер фабрики слушателей для обработки сообщений EventSimilarityAvro.
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, EventSimilarityAvro> eventsSimilarityListenerContainerFactory() {
        var factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(eventsSimilarityConsumerFactory()); // фабрика потребителей
        factory.setAutoStartup(false); // ручной старт контейнера
        factory.setBatchListener(false); // обрабатываются одиночные сообщения
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD); // подтверждения поступают по каждой записи
        return factory;
    }
}