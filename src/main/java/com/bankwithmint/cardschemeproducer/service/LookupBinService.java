package com.bankwithmint.cardschemeproducer.service;

import com.bankwithmint.cardschemeproducer.config.AppConfig;
import com.bankwithmint.cardschemeproducer.dao.CardScheme;
import com.bankwithmint.cardschemeproducer.model.LookupResponse;
import com.bankwithmint.cardschemeproducer.model.Minted;
import com.bankwithmint.cardschemeproducer.model.Payload;
import com.bankwithmint.cardschemeproducer.repository.CardSchemeRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Iterator;

@Service
public class LookupBinService {
    @Autowired
    RestTemplate restTemplate;

    @Autowired
    AppConfig appConfig;

    @Autowired
    KafkaProducerService producerService;

    private KafkaProducerService process;

    @Autowired
    CardSchemeRepository cardSchemeRepository;

    @Autowired
    public void setAsyncService(KafkaProducerService asyncService) {
        this.process = asyncService;
    }

    public ResponseEntity<LookupResponse> lookupBin(Long bin){
        LookupResponse lookupResponse = new LookupResponse();
        try {
            // call 3rd party API, https://binlist.net/ to search bin
            ResponseEntity<Minted> response = restTemplate.getForEntity(appConfig.getBinEndpoint + "/{bin}",
                    Minted.class,
                    Long.toString(bin));

            Minted minted = response.getBody();

            //construct payload object
            Payload payload = new Payload();
            payload.setScheme(minted.getScheme());
            payload.setType(minted.getType());
            payload.setBank(minted.getBank().getName());

            lookupResponse.setSuccess(true);
            lookupResponse.setPayload(payload);

            process.process(new Runnable() {
                @Override
                public void run() {
                    // update the number of times this bin has been searched
                    updateHitCount(bin, payload);

                    // write payload to kafka topic in the background
                    String kafkaTopic = appConfig.getKafkaTopic();
                    String message = jsonifyPayload(payload);
                    if (message != null)
                        producerService.sendMessage(kafkaTopic, message);
                }
            });

            // return response to user
            return new ResponseEntity<>(lookupResponse, HttpStatus.OK);
        }
        catch (Exception exception){
            return new ResponseEntity<>(lookupResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private String jsonifyPayload(Payload payload){
        // Creating Object of ObjectMapper define in Jackson api
        ObjectMapper objectMapper = new ObjectMapper();
        String message = "";
        try {
            // get Payload object as a json string
            message = objectMapper.writeValueAsString(payload);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return message;
    }

    private void updateHitCount(Long bin, Payload payload){
        // update the hit count of this bin
        Iterable<CardScheme> schemeIterable = cardSchemeRepository.findByBin(bin);
        Iterator<CardScheme> schemeIterator = schemeIterable.iterator();
        CardScheme cardScheme = new CardScheme();

        if (schemeIterator.hasNext()){
            // if this bin has been previously searched, update count
            cardScheme = schemeIterator.next();
            cardScheme.setBinCount(cardScheme.getBinCount() + 1);

            cardSchemeRepository.save(cardScheme);
        }
        else {
            // new bin search, save to db
            cardScheme.setBank(payload.getBank());
            cardScheme.setBin(bin);
            cardScheme.setScheme(payload.getScheme());
            cardScheme.setType(payload.getType());
            cardScheme.setBinCount(1);

            cardSchemeRepository.save(cardScheme);
        }
    }
}
