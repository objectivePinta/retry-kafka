package com.retryable.topic.demo.kafka;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.retrytopic.FixedDelayStrategy;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


@Slf4j
@RequiredArgsConstructor
@Component
public class RetryConsumer {

  LocalDateTime now;


  @RetryableTopic(
      attempts = "5",
      autoCreateTopics = "false",
      backoff = @Backoff(30_000),
      fixedDelayTopicStrategy = FixedDelayStrategy.SINGLE_TOPIC,
      listenerContainerFactory = "retryKafkaListenerContainerFactory"
  )
  @KafkaListener(id = "retry-consumer",
                 topics = "my-retry-topic",
                 containerFactory = "retryKafkaListenerContainerFactory",
                 groupId = "retry-demo-2")
  public void handleMessage (
      String message,
      @Header(KafkaHeaders.RECEIVED_TOPIC) String topic
  ) {

    log.info("Received message: {} from topic: {}", message, topic);
    if (now == null) {
      now = LocalDateTime.now();
      System.out.println("attempt at:" + now.toString());
    } else {
      System.out.println("minutes between attempts:" + Duration.between(LocalDateTime.now(), now).toSeconds());
    }
    throw new RuntimeException("Test exception");
  }


  @DltHandler
  public void handleDlt (
      String message,
      @Header(KafkaHeaders.RECEIVED_TOPIC) String topic
  ) {

    log.info("Message: {} handled by dlq topic: {}", message, topic);
  }
}
