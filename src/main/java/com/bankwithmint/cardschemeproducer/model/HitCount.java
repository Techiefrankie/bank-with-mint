package com.bankwithmint.cardschemeproducer.model;

import java.util.HashMap;
import java.util.List;

public class HitCount {
    private boolean success;
    private int start;
    private  int limit;
    private  int size;
    List<HashMap<String, Integer>> payload;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public List<HashMap<String, Integer>> getPayload() {
        return payload;
    }

    public void setPayload(List<HashMap<String, Integer>> payload) {
        this.payload = payload;
    }
}
