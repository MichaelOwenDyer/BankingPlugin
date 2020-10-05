package com.monst.bankingplugin.lang;

public enum Placeholder {

    PRICE (true),
    AMOUNT (true),
    AMOUNT_REMAINING (true),
    PLAYER_BALANCE (true),
    ACCOUNT_BALANCE (true),
    BANK,
    PLAYER,
    OWNER,
    NUMBER_OF_ACCOUNTS,
    NUMBER_OF_BANKS,
    NUMBER,
    STRING,
    MULTIPLIER,
    ERROR,
    LIMIT,
    PROPERTY,
    VALUE,
    COMMAND,
    VERSION;

    private final boolean isMoney;

    Placeholder() {
        this(false);
    }

    Placeholder(boolean isMoney) {
        this.isMoney = isMoney;
    }

    @Override
    public String toString() {
        return "%" + name() + "%";
    }

    public boolean isMoney() {
        return isMoney;
    }

}
