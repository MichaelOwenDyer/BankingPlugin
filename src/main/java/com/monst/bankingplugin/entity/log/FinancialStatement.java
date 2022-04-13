package com.monst.bankingplugin.entity.log;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@MappedSuperclass
public abstract class FinancialStatement {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault());

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;
    Instant timestamp;

    public FinancialStatement() {}

    FinancialStatement(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public Integer getID() {
        return id;
    }

    public Instant getInstant() {
        return timestamp;
    }

    public String getTimestamp() {
        return DATE_FORMAT.format(timestamp);
    }

    @Override
    public String toString() {
        return "FinancialStatement{" +
                "id=" + id +
                ", time=" + timestamp +
                '}';
    }

}
