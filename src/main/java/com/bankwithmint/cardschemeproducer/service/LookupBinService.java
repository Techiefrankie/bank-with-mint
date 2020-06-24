package com.bankwithmint.cardschemeproducer.service;

import com.bankwithmint.cardschemeproducer.config.AppConfig;
import com.bankwithmint.cardschemeproducer.dao.CardScheme;
import com.bankwithmint.cardschemeproducer.model.HitCount;
import com.bankwithmint.cardschemeproducer.model.LookupResponse;
import com.bankwithmint.cardschemeproducer.model.Minted;
import com.bankwithmint.cardschemeproducer.model.Payload;
import com.bankwithmint.cardschemeproducer.repository.CardSchemeRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

@Service
public class LookupBinService {
    @Autowired
    RestTemplate restTemplate;

    @Autowired
    AppConfig appConfig;

    private KafkaProducerService process;

    @Autowired
    CardSchemeRepository cardSchemeRepository;

    @Autowired
    public void setAsyncService(KafkaProducerService asyncService) {
        this.process = asyncService;
    }

    public ResponseEntity<LookupResponse> lookupBin(String bin){
        LookupResponse lookupResponse = new LookupResponse();

        // check if this bin has been verified previously and cached to the db
        CardScheme cardScheme = cardSchemeRepository.findByBin(bin);
        Payload payload = null;

        if (cardScheme != null){
            // construct payload from cardscheme object
            payload = new Payload();
            payload.setScheme(cardScheme.getScheme());
            payload.setType(cardScheme.getType());
            payload.setBank(cardScheme.getBank());
        }
        else {
            payload = getFromBinList(bin);
        }

        lookupResponse.success = payload != null;
        lookupResponse.payload = payload;

        try {
            Payload finalPayload = payload;
            process.process(new Runnable() {
                @Override
                public void run() {
                    // update the number of times this bin has been searched
                    updateHitCount(bin, finalPayload);

                    // write payload to kafka topic in the background
                    String kafkaTopic = appConfig.getKafkaTopic();
                    String message = jsonifyPayload(finalPayload);
                    if (message != null)
                        process.sendMessage(kafkaTopic, message);
                }
            });

            // return response to user
            return new ResponseEntity<>(lookupResponse, HttpStatus.OK);
        }
        catch (Exception exception){
            return new ResponseEntity<>(lookupResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private Payload getFromBinList(String bin){
        Payload payload = null;
        try {
            // call 3rd party API, https://binlist.net/ to search bin
            ResponseEntity<Minted> response = restTemplate.getForEntity(appConfig.getBinEndpoint + "/{bin}",
                    Minted.class, bin);
            if (response.getBody() != null){
                Minted minted = response.getBody();

                // parse response to construct payload object
                payload = new Payload();
                payload.setScheme(minted.getScheme());
                payload.setType(minted.getType());
                payload.setBank(minted.getBank().getName());
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }

        return payload;
    }

    public String jsonifyPayload(Payload payload){
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

    public void updateHitCount(String bin, Payload payload){
        // update the hit count of this bin
        CardScheme cardScheme = cardSchemeRepository.findByBin(bin);

        if (cardScheme != null){
            // this bin has been previously searched, update count
            cardScheme.setBinCount(cardScheme.getBinCount() + 1);

            cardSchemeRepository.save(cardScheme);
        }
        else {
            // new bin search, save to db
            cardScheme = new CardScheme();
            cardScheme.setBank(payload.getBank());
            cardScheme.setBin(bin);
            cardScheme.setScheme(payload.getScheme());
            cardScheme.setType(payload.getType());
            cardScheme.setBinCount(1);

            cardSchemeRepository.save(cardScheme);
        }
    }

    public HitCount getHitCounts(int start, int limit){
        HitCount hitCount = new HitCount();
        hitCount.setSuccess(true);
        hitCount.setStart(start);
        hitCount.setLimit(limit);
        hitCount.setSize((int) cardSchemeRepository.count());

        Pageable pageable = PageRequest.of(start,limit);
        // load first start to limit records from the database
        Iterator<CardScheme> schemeIterator = cardSchemeRepository.findAll(pageable).iterator();
        List<HashMap<String, Integer>> hitCounts = new ArrayList<>();

        while (schemeIterator.hasNext()){
            // construct the payload
            CardScheme cardScheme = schemeIterator.next();
            HashMap<String, Integer> binCount = new HashMap<>();
            binCount.put(String.valueOf(cardScheme.getBin()), cardScheme.getBinCount());
            hitCounts.add(binCount);
        }
        hitCount.setPayload(hitCounts);

        return hitCount;
    }
}
