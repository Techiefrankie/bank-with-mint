package com.bankwithmint.cardschemeproducer;

import com.bankwithmint.cardschemeproducer.controller.CardSchemeController;
import com.bankwithmint.cardschemeproducer.dao.CardScheme;
import com.bankwithmint.cardschemeproducer.model.HitCount;
import com.bankwithmint.cardschemeproducer.model.LookupResponse;
import com.bankwithmint.cardschemeproducer.model.Payload;
import com.bankwithmint.cardschemeproducer.repository.CardSchemeRepository;
import com.bankwithmint.cardschemeproducer.service.LookupBinService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
class CardSchemeProducerApplicationTests {

    @Autowired
    CardSchemeController cardSchemeController;

    @Autowired
    CardSchemeRepository cardSchemeRepository;

    @Test
    public void testVerifyBin() {
        ResponseEntity<LookupResponse> entity = cardSchemeController.verify("545423");
        Assertions.assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(entity.getBody()).hasFieldOrPropertyWithValue("success", true);
    }

    @Test
    public void testHitCounts(){
        ResponseEntity<LookupResponse> entity = cardSchemeController.verify("545423");
        ResponseEntity<HitCount> hitCountResponseEntity = cardSchemeController.hitCount(0, 3);
        Assertions.assertThat(hitCountResponseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(hitCountResponseEntity.getBody()).hasFieldOrPropertyWithValue("success", true);
        Assertions.assertThat(hitCountResponseEntity.getBody()).hasFieldOrPropertyWithValue("start", 0);
    }

    @Autowired
    LookupBinService lookupBinService;

    @Test
    public void testJsonify(){
        // construct payload object
        Payload payload = new Payload();
        payload.setScheme("visa");
        payload.setType("debit");
        payload.setBank("UBS");

        Assertions.assertThat(lookupBinService.jsonifyPayload(payload)).isNotEmpty();
    }

    @Test
    public  void testCreateCardScheme(){
        // new bin search, save to db
        CardScheme cardScheme = new CardScheme();
        cardScheme.setBank("UBS");
        cardScheme.setBin("545423");
        cardScheme.setScheme("visa");
        cardScheme.setType("debit");
        cardScheme.setBinCount(1);
        cardSchemeRepository.save(cardScheme);

        Assertions.assertThat(cardSchemeRepository.findByBin("545423"))
                .hasFieldOrPropertyWithValue("bin", "545423");
    }

}
