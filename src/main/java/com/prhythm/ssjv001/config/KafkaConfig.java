package com.prhythm.ssjv001.config;

import com.prhythm.ssjv001.config.vo.KafkaServerProperties;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;

import java.util.HashMap;

@Configuration
@EnableConfigurationProperties
@ConditionalOnProperty(value = "com.prhythm.ssjv001.kafka.exchange.enabled", havingValue = "true")
public class KafkaConfig {

    @Bean
    @ConfigurationProperties("com.prhythm.ssjv001.kafka.exchange")
    public KafkaServerProperties kafkaServerConfig() {
        return new KafkaServerProperties();
    }

    @Bean
    public KafkaAdmin kafkaAdmin(KafkaServerProperties kafkaServerProperties) {
        var config = new HashMap<String, Object>();
        config.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServerProperties.getBootstrapAddress());
        return new KafkaAdmin(config);
    }

    @Bean
    public NewTopic simpleCommandTopic() {
        return new NewTopic(KafkaServerProperties.TOPIC_DIRECTIVE_TICKET, 1, (short) 1);
    }

    @Bean
    public NewTopic simpleSolverTopic() {
        return new NewTopic(KafkaServerProperties.TOPIC_DIRECTIVE_RESPONSE, 1, (short) 1);
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
    public ConsumerFactory<String, String> consumerFactory(KafkaServerProperties kafkaServerProperties) {
        var config = new HashMap<String, Object>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServerProperties.getBootstrapAddress());
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return new DefaultKafkaConsumerFactory<>(config);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> concurrentKafkaListenerContainerFactory(ConsumerFactory<String, String> consumerFactory) {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, String>();
        factory.setConsumerFactory(consumerFactory);
        return factory;
    }

}
