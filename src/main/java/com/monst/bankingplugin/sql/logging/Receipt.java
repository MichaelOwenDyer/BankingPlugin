package com.monst.bankingplugin.sql.logging;

import java.text.SimpleDateFormat;

public abstract class Receipt {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMM dd, hh:mm a");

    long time;

    public Receipt() {

    }

    Receipt(long time) {
        this.time = time;
    }

    public abstract int getID();

    public long getTime() {
        return time;
    }

    public String getTimeFormatted() {
        return DATE_FORMAT.format(getTime());
    }

}
