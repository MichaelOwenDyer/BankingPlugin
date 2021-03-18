package com.monst.bankingplugin.sql.logging;

import java.text.SimpleDateFormat;

public abstract class Receipt {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, hh:mm a");

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
        return dateFormat.format(getTime());
    }

}
