package com.retryable.topic.demo.kafka;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SslConfigs;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;


@Configuration
public class KafkaProducerConfiguration {

  private final KafkaCustomProperties kafkaProperties;


  public KafkaProducerConfiguration (KafkaCustomProperties kafkaProperties) {

    this.kafkaProperties = kafkaProperties;
  }


  @Bean
  public KafkaTemplate<Object, Object> kafkaTemplate (
      ProducerFactory<Object, Object> producerFactory
  ) {

    return new KafkaTemplate<>(producerFactory);
  }


  @Bean
  public ProducerFactory<Object, Object> producerFactory () {
    ObjectMapper mapper = new ObjectMapper();
    DefaultKafkaProducerFactory<Object, Object> factory = new DefaultKafkaProducerFactory<>(
        producerConfigProperties(),
        new JsonSerializer<>(mapper),
        new JsonSerializer<>(mapper)
    );

    factory.setProducerPerConsumerPartition(true);

    return factory;
  }


  private Map<String, Object> producerConfigProperties () {

    Map<String, Object> config = new HashMap<>();

    config.put(
        ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG,
        kafkaProperties.getProducer().getRequestTimeout()
    );
    config.put(
        ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
        kafkaProperties.getBootstrapServers()
    );
    config.put(
        ProducerConfig.COMPRESSION_TYPE_CONFIG,
        kafkaProperties.getProducer().getCompressionType()
    );
    config.put(
        ProducerConfig.BATCH_SIZE_CONFIG,
        (int) kafkaProperties.getProducer().getBatchSize().toBytes()
    );
    config.put(ProducerConfig.LINGER_MS_CONFIG, kafkaProperties.getProducer().getLinger());
    config.put(ProducerConfig.RETRIES_CONFIG, kafkaProperties.getProducer().getRetries());
    config.put(
        ProducerConfig.RETRY_BACKOFF_MS_CONFIG,
        kafkaProperties.getProducer().getRetryBackoff()
    );
    config.put(
        ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG,
        kafkaProperties.getProducer().getProperties().get("enable.idempotence")
    );

    if (kafkaProperties.getSsl() != null && kafkaProperties.getSsl().getEnabled()) {
      config.put(
          KafkaCustomProperties.SECURITY_PROTOCOL,
          kafkaProperties.getSsl().getSecurityProtocol()
      );
      config.put(
          SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG,
          kafkaProperties.getSsl().getTrustStoreLocationPath()
      );
      config.put(
          SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG,
          kafkaProperties.getSsl().getKeyStoreLocationPath()
      );
      config.put(
          SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG,
          kafkaProperties.getSsl().getTrustStorePassword()
      );
      config.put(
          SslConfigs.SSL_KEY_PASSWORD_CONFIG,
          kafkaProperties.getSsl().getKeyStorePassword()
      );
      config.put(
          SslConfigs.SSL_KEYSTORE_TYPE_CONFIG,
          kafkaProperties.getSsl().getKeyStoreType()
      );
      config.put(
          SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG,
          kafkaProperties.getSsl().getTrustStoreType()
      );
      config.put(
          SslConfigs.SSL_PROTOCOL_CONFIG,
          kafkaProperties.getSsl().getProtocol()
      );
      config.put(
          SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG,
          kafkaProperties.getSsl().getKeyStorePassword()
      );
    }

    return config;
  }
}
