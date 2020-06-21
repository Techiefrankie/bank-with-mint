package com.bankwithmint.cardschemeproducer.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class KafkaProducerService {
    private static final Logger logger = LoggerFactory.getLogger(KafkaProducerService.class);
    private ExecutorService executorService;


    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    public void sendMessage(String topic, String message) {
        logger.info(String.format("[+] ===> Producing message ==> %s", message));
        this.kafkaTemplate.send(topic, message);
    }

    @PostConstruct
    private void create() {
        executorService = Executors.newSingleThreadExecutor();
    }

    public void process(Runnable operation) {
        // submit runnable operation
        executorService.submit(operation);
    }

    @PreDestroy
    private void destroy() {
        executorService.shutdown();
    }
}
