package com.monst.bankingplugin.entity.log;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public abstract class FinancialStatement {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault());

    Integer id;
    final Instant instant;
    
    public FinancialStatement(int id, Instant instant) {
        this.id = id;
        this.instant = instant;
    }

    FinancialStatement(Instant instant) {
        this.instant = instant;
    }

    public Integer getID() {
        return id;
    }

    public Instant getInstant() {
        return instant;
    }

    public String getTimestamp() {
        return DATE_FORMAT.format(instant);
    }

    @Override
    public String toString() {
        return "FinancialStatement{" +
                "id=" + id +
                ", time=" + instant +
                '}';
    }

}
