package com.anicaazhu.lucenedemo.model;

public class SearchResult {
    public String pickup;
    public String dropoff;
    public double totalAmount;
    public String datetime;

    public SearchResult(String pickup, String dropoff, double totalAmount, String datetime) {
        this.pickup = pickup;
        this.dropoff = dropoff;
        this.totalAmount = totalAmount;
        this.datetime = datetime;
    }
}
