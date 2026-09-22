package com.example.lab2.domain.model;

public class Patient {
    private final String fio;
    private final String dobString;
    private final Long dobMs;
    private final String omsPolicy;
    private final String phone;

    public Patient(String fio, String dobString, Long dobMs, String omsPolicy, String phone) {
        this.fio = fio;
        this.dobString = dobString;
        this.dobMs = dobMs;
        this.omsPolicy = omsPolicy;
        this.phone = phone;
    }

    public String getFio() { return fio; }
    public String getDobString() { return dobString; }
    public Long getDobMs() { return dobMs; }
    public String getOmsPolicy() { return omsPolicy; }
    public String getPhone() { return phone; }
}
