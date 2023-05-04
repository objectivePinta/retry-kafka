package com.retryable.topic.demo.kafka;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;


@ConfigurationProperties(
    prefix = "spring.kafka"
)
@Configuration
@Primary
@Setter
@Getter
public class KafkaCustomProperties extends KafkaProperties {

  public static final String SECURITY_PROTOCOL = "security.protocol";

  private final CustomSsl ssl = new CustomSsl();

  private CustomConsumer consumer = new CustomConsumer();

  private CustomProducer producer = new CustomProducer();

  @Setter
  @Getter
  @NoArgsConstructor
  public static final class CustomProducer extends Producer {

    private Integer linger;

    private Integer retryBackoff;

    private Integer requestTimeout;
  }

  @Setter
  @Getter
  @NoArgsConstructor
  public static final class CustomSsl extends Ssl {

    private Boolean enabled;

    private String securityProtocol;

    private String trustStoreLocationPath;

    private String keyStoreLocationPath;
  }

  @Setter
  @Getter
  @NoArgsConstructor
  public static final class CustomConsumer extends Consumer {

    private Boolean logPayload;

    private Integer connectionMaxIdle;

    private Integer maxPollInterval;

    private Integer requestTimeout;

    private Integer defaultApiTimeout;

    private Integer metadataMaxAge;

    private Integer sessionTimeout;

    private Integer maxRetries;

    private Double retryBackoffMultiplier;

    private Long retryBackoffMaxInterval;

  }
}
