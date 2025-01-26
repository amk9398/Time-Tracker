package com.aaronkersten.timetracker;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class Task {
    private IntegerProperty number;
    private StringProperty startTime;
    private StringProperty endTime;
    private StringProperty description;
    private StringProperty chargeCode;

    public Task(int number, String startTime, String endTime, String description, String chargeCode) {
        this.number = new SimpleIntegerProperty(number);
        this.startTime = new SimpleStringProperty(startTime);
        this.endTime = new SimpleStringProperty(endTime);
        this.description = new SimpleStringProperty(description);
        this.chargeCode = new SimpleStringProperty(chargeCode);
    }

    public int getNumber() {
        return number.get();
    }

    public String getStartTime() {
        return startTime.get();
    }

    public String getEndTime() {
        return endTime.get();
    }

    public String getDescription() {
        return description.get();
    }

    public String getChargeCode() {
        return chargeCode.get();
    }

    public double getDuration() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

        if (startTime.get() == null || endTime.get() == null) {
            return 0;
        }

        String startTime = this.startTime.get();
        if (startTime.length() == 4) {
            startTime = "0" + startTime;
        }

        String endTime = this.endTime.get();
        if (endTime.length() == 4) {
            endTime = "0" + endTime;
        }

        try {
            LocalTime start = LocalTime.parse(startTime, formatter);
            LocalTime end = LocalTime.parse(endTime, formatter);
            return ChronoUnit.MINUTES.between(start, end);
        } catch (Exception e) {
            return 0;
        }
    }

    public void setNumber(int number) {
        this.number = new SimpleIntegerProperty(number);
    }

    public void setStartTime(String startTime) {
        this.startTime = new SimpleStringProperty(startTime);
    }

    public void setEndTime(String endTime) {
        this.endTime = new SimpleStringProperty(endTime);
    }

    public void setDescription(String description) {
        this.description = new SimpleStringProperty(description);
    }

    public void setChargeCode(String chargeCode) {
        this.chargeCode = new SimpleStringProperty(chargeCode);
    }

    @Override
    public String toString() {
        return String.join("\t", getStartTime(), getEndTime(), getDescription(), getChargeCode());
    }
}
