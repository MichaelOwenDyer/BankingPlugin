package com.monst.bankingplugin.sql.logging;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;

public abstract class Receipt implements Loggable {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yy hh:mm a");

    BigDecimal amount;
    long time;

    public Receipt() {

    }

    Receipt(BigDecimal amount, long time) {
        this.amount = amount;
        this.time = time;
    }

    public abstract int getID();

    public BigDecimal getAmount() {
        return amount;
    }

    public long getTime() {
        return time;
    }

    public String getTimeFormatted() {
        return dateFormat.format(getTime());
    }

    public abstract int getBankID();

}
