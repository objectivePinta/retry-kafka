package com.retryable.topic.demo.kafka;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.retrytopic.RetryTopicConfiguration;
import org.springframework.kafka.retrytopic.RetryTopicConfigurationBuilder;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;
import org.springframework.kafka.support.converter.MessageConverter;
import org.springframework.kafka.support.converter.StringJsonMessageConverter;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.HashMap;
import java.util.Map;


@RequiredArgsConstructor
@Configuration
@Slf4j
public class KafkaConsumerConfiguration {

  private final KafkaCustomProperties kafkaProperties;


  @Bean
  public MessageConverter messageConverter () {

    return new StringJsonMessageConverter();
  }


  @Bean
  @Primary
  public ConcurrentKafkaListenerContainerFactory<String, String> batchKafkaListenerContainerFactory
      (
          MessageConverter messageConverter
      ) {

    ConcurrentKafkaListenerContainerFactory<String, String> factory =
        new ConcurrentKafkaListenerContainerFactory<>();

    factory.setBatchListener(true);
    factory.setMessageConverter(messageConverter);
    factory.setCommonErrorHandler(defaultErrorHandler());
    factory.setConsumerFactory(new DefaultKafkaConsumerFactory<>(generateConsumerConfig()));
    factory.setRecordFilterStrategy(consumerRecord -> {
      if (Boolean.TRUE.equals(kafkaProperties.getConsumer().getLogPayload())) {
        log.info(consumerRecord.toString());
      }
      return false;
    });

    return factory;
  }


  @Bean
  @Qualifier("retryKafkaListenerContainerFactory")
  public ConcurrentKafkaListenerContainerFactory<String, String> retryKafkaListenerContainerFactory
      (
          MessageConverter messageConverter
      ) {

    ConcurrentKafkaListenerContainerFactory<String, String> factory =
        new ConcurrentKafkaListenerContainerFactory<>();

    factory.setBatchListener(false);
    factory.setMessageConverter(messageConverter);
//    factory.setCommonErrorHandler(defaultErrorHandler());
    factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);

    factory.setConsumerFactory(new DefaultKafkaConsumerFactory<>(generateConsumerConfig()));
    factory.setRecordFilterStrategy(consumerRecord -> {
      if (Boolean.TRUE.equals(kafkaProperties.getConsumer().getLogPayload())) {
        log.info(consumerRecord.toString());
      }
      return false;
    });

    return factory;
  }


  @Bean
  public DefaultErrorHandler defaultErrorHandler () {

    ExponentialBackOffWithMaxRetries backOff
        = new ExponentialBackOffWithMaxRetries(kafkaProperties.getConsumer().getMaxRetries());
    backOff.setMultiplier(kafkaProperties.getConsumer().getRetryBackoffMultiplier());
    backOff.setMaxInterval(kafkaProperties.getConsumer().getRetryBackoffMaxInterval());
    return new DefaultErrorHandler(backOff);
  }


  private Map<String, Object> generateConsumerConfig () {

    Map<String, Object> config = new HashMap<>();

    config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
    config.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaProperties.getConsumer().getGroupId());
    config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    config.put(
        ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,
        kafkaProperties.getConsumer().getEnableAutoCommit()
    );
    config.put(
        ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
        kafkaProperties.getConsumer().getAutoOffsetReset()
    );
    config.put(
        ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG,
        (int) kafkaProperties.getConsumer().getAutoCommitInterval().toMillis()
    );
    config.put(
        ConsumerConfig.CONNECTIONS_MAX_IDLE_MS_CONFIG,
        kafkaProperties.getConsumer().getConnectionMaxIdle()
    );
    config.put(
        ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG,
        kafkaProperties.getConsumer().getMaxPollInterval()
    );
    config.put(
        ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG,
        kafkaProperties.getConsumer().getRequestTimeout()
    );
    config.put(
        ConsumerConfig.DEFAULT_API_TIMEOUT_MS_CONFIG,
        kafkaProperties.getConsumer().getDefaultApiTimeout()
    );
    config.put(
        ConsumerConfig.METADATA_MAX_AGE_CONFIG,
        kafkaProperties.getConsumer().getMetadataMaxAge()
    );
    config.put(
        ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG,
        kafkaProperties.getConsumer().getSessionTimeout()
    );

    setSslProperties(config);

    return config;
  }


  private void setSslProperties (Map<String, Object> config) {

    if (kafkaProperties.getSsl() != null && kafkaProperties.getSsl().getEnabled()) {
      config.put(
          KafkaCustomProperties.SECURITY_PROTOCOL,
          kafkaProperties.getSsl().getSecurityProtocol()
      );
      config.put(
          SslConfigs.SSL_PROTOCOL_CONFIG,
          kafkaProperties.getSsl().getProtocol()
      );

      config.put(
          SslConfigs.SSL_KEYSTORE_TYPE_CONFIG,
          kafkaProperties.getSsl().getKeyStoreType()
      );
      config.put(
          SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG,
          kafkaProperties.getSsl().getKeyStoreLocationPath()
      );
      config.put(
          SslConfigs.SSL_KEY_PASSWORD_CONFIG,
          kafkaProperties.getSsl().getKeyStorePassword()
      );
      config.put(
          SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG,
          kafkaProperties.getSsl().getKeyStorePassword()
      );

      config.put(
          SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG,
          kafkaProperties.getSsl().getTrustStoreType()
      );
      config.put(
          SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG,
          kafkaProperties.getSsl().getTrustStoreLocationPath()
      );
      config.put(
          SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG,
          kafkaProperties.getSsl().getTrustStorePassword()
      );
    }
  }


  @Bean
  TaskScheduler sched () {

    return new ThreadPoolTaskScheduler();
  }


  @Bean
  public RetryTopicConfiguration myRetryTopic (KafkaTemplate<Object, Object> template) {

    return RetryTopicConfigurationBuilder
        .newInstance()
        .doNotAutoCreateRetryTopics()
        .listenerFactory("retryKafkaListenerContainerFactory")
        .fixedBackOff(30000)
        .maxAttempts(5)
        .useSingleTopicForFixedDelays()
        .create(template);
  }


  @Bean
  public NewTopic compactTopicExample () {

    return TopicBuilder.name("first-topic")
        .partitions(6)
        .replicas(3)
        .compact()
        .build();
  }


  @Bean
  public NewTopic retryTopic () {

    return TopicBuilder.name("my-retry-topic")
        .partitions(6)
        .replicas(3)
        .compact()
        .build();
  }
}
