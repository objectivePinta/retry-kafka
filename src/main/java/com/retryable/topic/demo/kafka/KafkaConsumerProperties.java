package com.retryable.topic.demo.kafka;


import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public abstract class KafkaConsumerProperties {

  private String name;

  private String concurrency;

  private String autoStartup;

  private String maxPollRecords;

  private String maxPartitionFetchBytes;

  private String fetchMaxWaitMs;

  private String clientIdPrefix;
}
