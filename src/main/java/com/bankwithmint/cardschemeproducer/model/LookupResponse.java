package com.bankwithmint.cardschemeproducer.model;

public class LookupResponse {
    public LookupResponse() {
    }

    public boolean success;
    public Payload payload;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Payload getPayload() {
        return payload;
    }

    public void setPayload(Payload payload) {
        this.payload = payload;
    }
}
