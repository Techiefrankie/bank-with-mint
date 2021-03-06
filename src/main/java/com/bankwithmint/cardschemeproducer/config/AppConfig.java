package com.bankwithmint.cardschemeproducer.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.client.RestTemplate;

@Configuration
@PropertySource("classpath:application.properties")
@ConfigurationProperties("")
public class AppConfig {

    @Value("${mint.endpoint}")
    public String getBinEndpoint;

    @Value("${spring.kafka.bootstrap-servers}")
    private String kafkaBootstrapServers;

    @Value("${spring.kafka.template.default-topic}")
    public String kafkaTopic;


    public String getGetBinEndpoint() {
        return getBinEndpoint;
    }

    public void setGetBinEndpoint(String getBinEndpoint) {
        this.getBinEndpoint = getBinEndpoint;
    }

    public String getKafkaBootstrapServers() {
        return kafkaBootstrapServers;
    }

    public void setKafkaBootstrapServers(String kafkaBootstrapServers) {
        this.kafkaBootstrapServers = kafkaBootstrapServers;
    }

    public String getKafkaTopic() {
        return kafkaTopic;
    }

    public void setKafkaTopic(String kafkaTopic) {
        this.kafkaTopic = kafkaTopic;
    }


    @Bean
    public RestTemplate restTemplate(){
        return new RestTemplate();
    }
}
