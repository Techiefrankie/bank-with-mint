package com.bankwithmint.cardschemeproducer.controller;

import com.bankwithmint.cardschemeproducer.model.LookupResponse;
import com.bankwithmint.cardschemeproducer.service.LookupBinService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/card-scheme")
public class CardSchemeController {
    @Autowired
    LookupBinService lookupBinService;

    @GetMapping(value = "/verify/{bin}", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LookupResponse> verify(@PathVariable Long bin){
        return lookupBinService.lookupBin(bin);
    }

}
