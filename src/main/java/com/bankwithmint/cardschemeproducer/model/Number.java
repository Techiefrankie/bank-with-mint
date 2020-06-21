package com.bankwithmint.cardschemeproducer.model;

public class Number {
    private int length;
    private boolean luhn;

    public Number() {
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public boolean isLuhn() {
        return luhn;
    }

    public void setLuhn(boolean luhn) {
        this.luhn = luhn;
    }
}
