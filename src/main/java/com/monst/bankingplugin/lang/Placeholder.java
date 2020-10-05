package com.monst.bankingplugin.lang;

public enum Placeholder {

    PRICE (true),
    AMOUNT (true),
    AMOUNT_REMAINING (true),
    PLAYER_BALANCE (true),
    ACCOUNT_BALANCE (true),
    BANK_NAME,
    ACCOUNT_NAME,
    PLAYER,
    NUMBER_OF_ACCOUNTS,
    NUMBER_OF_BANKS,
    NUMBER,
    STRING,
    ERROR,
    LIMIT,
    PROPERTY,
    VALUE,
    PREVIOUS_VALUE,
    WORLD,
    ACCOUNT_COMMAND,
    BANK_COMMAND,
    CONTROL_COMMAND,
    VERSION;

    private String placeholder = null;
    private final boolean isMoney;

    Placeholder() {
        this(false);
    }

    Placeholder(boolean isMoney) {
        this.isMoney = isMoney;
    }

    @Override
    public String toString() {
        return placeholder != null ? placeholder : (placeholder = "%" + name() + "%");
    }

    public boolean isMoney() {
        return isMoney;
    }

}
