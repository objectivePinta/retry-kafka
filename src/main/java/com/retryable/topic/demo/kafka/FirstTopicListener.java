package com.retryable.topic.demo.kafka;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;


@Slf4j
@RequiredArgsConstructor
@Component
public class FirstTopicListener {

  private final KafkaTemplate<Object, Object> kafkaTemplate;

  LocalDateTime now;


  //  @PostConstruct
  void sendMessageAtStartup () {

    kafkaTemplate.send("first-topic", List.of("some"));
  }


  @KafkaListener(
      containerFactory = "kafkaListenerContainerFactory",
      clientIdPrefix = "demo",
      topics = "first-topic",
      concurrency = "1",
      autoStartup = "true",
      groupId = "retry-demo-1",
      properties = {
          ConsumerConfig.MAX_POLL_RECORDS_CONFIG
              + "1000",
          ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG
              + "100000",
          ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG
              + "5000"
      })
  public void consumeInvoices (@Payload List<String> payload) {

    //here I simulate a fail and I put the message on the retry topic
    kafkaTemplate.send("my-retry-topic", payload.get(0));

  }
}
