package com.monst.bankingplugin.sql.logging;

import java.math.BigDecimal;

public abstract class Receipt {

    BigDecimal amount;
    long time;

    public Receipt() {

    }

    Receipt(BigDecimal amount, long time) {
        this.amount = amount;
        this.time = time;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public long getTime() {
        return time;
    }

    public abstract int getBankID();

}
