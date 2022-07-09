package com.prhythm.ssjv001.config;

import com.prhythm.ssjv001.config.vo.KafkaServerProperties;
import com.prhythm.ssjv001.message.KafkaGroupAdvisor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ConsumerAwareRebalanceListener;

import java.util.Collection;
import java.util.HashMap;
import java.util.stream.Collectors;

import static com.prhythm.ssjv001.config.vo.KafkaServerProperties.TOPIC_DIRECTIVE_RESPONSE;
import static com.prhythm.ssjv001.config.vo.KafkaServerProperties.TOPIC_DIRECTIVE_TICKET;

@Slf4j
@Configuration
@EnableConfigurationProperties
@ConditionalOnProperty(value = "com.prhythm.ssjv001.kafka.exchange.enabled", havingValue = "true")
public class KafkaConfig {

    @Bean
    @ConfigurationProperties("com.prhythm.ssjv001.kafka.exchange")
    public KafkaServerProperties KafkaServerProperties() {
        return new KafkaServerProperties();
    }

    @Bean
    public KafkaAdmin kafkaAdmin(KafkaServerProperties kafkaServerProperties) {
        var config = new HashMap<String, Object>();
        config.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServerProperties.getBootstrapAddress());
        return new KafkaAdmin(config);
    }

    @Bean
    public NewTopic topicDirectiveTicket() {
        return new NewTopic(TOPIC_DIRECTIVE_TICKET, 3, (short) 1);
    }

    @Bean
    public NewTopic topicDirectiveResponse() {
        return new NewTopic(TOPIC_DIRECTIVE_RESPONSE, 10, (short) 1);
    }

    @Bean
    public ProducerFactory<String, String> producerFactory(KafkaServerProperties kafkaServerProperties) {
        var config = new HashMap<String, Object>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServerProperties.getBootstrapAddress());
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate(ProducerFactory<String, String> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    public KafkaGroupAdvisor kafkaGroupAdvisor() {
        return new KafkaGroupAdvisor();
    }

    @Bean
    public ConsumerAwareRebalanceListener consumerAwareRebalanceListener(KafkaGroupAdvisor kafkaGroupAdvisor) {
        return new ConsumerAwareRebalanceListener() {
            @Override
            public void onPartitionsLost(@NonNull Consumer<?, ?> consumer, @NonNull Collection<TopicPartition> partitions) {
                log.info("partition lost: {}", partitions);
                for (TopicPartition partition : partitions) {
                    kafkaGroupAdvisor.unregister(partition.topic(), partition.partition());
                }
            }

            @Override
            public void onPartitionsAssigned(@NonNull Consumer<?, ?> consumer, @NonNull Collection<TopicPartition> partitions) {
                log.info("partition assigned: {}", partitions);
                partitions.stream()
                        .collect(Collectors.groupingBy(TopicPartition::topic))
                        .forEach((topic, group) -> kafkaGroupAdvisor.register(
                                topic,
                                group.stream().mapToInt(TopicPartition::partition).toArray()
                        ));
            }
        };
    }

    @Bean
    public ConsumerFactory<String, String> consumerFactory(KafkaServerProperties kafkaServerProperties) {
        var config = new HashMap<String, Object>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServerProperties.getBootstrapAddress());
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return new DefaultKafkaConsumerFactory<>(config);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> concurrentKafkaListenerContainerFactory(ConsumerFactory<String, String> consumerFactory,
                                                                                                           ConsumerAwareRebalanceListener consumerAwareRebalanceListener) {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, String>();
        factory.setConsumerFactory(consumerFactory);
        factory.getContainerProperties().setConsumerRebalanceListener(consumerAwareRebalanceListener);
        return factory;
    }

}
