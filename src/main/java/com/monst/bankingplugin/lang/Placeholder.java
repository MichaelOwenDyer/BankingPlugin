package com.monst.bankingplugin.lang;

public enum Placeholder {

    PRICE,
    AMOUNT,
    AMOUNT_REMAINING,
    PLAYER_BALANCE,
    ACCOUNT_BALANCE,
    BANK_NAME,
    ACCOUNT_ID,
    NAME,
    PLAYER,
    NUMBER_OF_ACCOUNTS,
    NUMBER_OF_BANKS,
    BANK_SIZE,
    MINIMUM,
    MAXIMUM,
    DIFFERENCE,
    NUMBER,
    INTEREST_MULTIPLIER,
    INTEREST_MULTIPLIER_STAGE,
    INPUT,
    LIMIT,
    PROPERTY,
    POLICY,
    VALUE,
    PREVIOUS_VALUE,
    PATTERN,
    WORLD,
    COMMAND,
    VERSION,
    URL;

    @Override
    public String toString() {
        return "%" + name() + "%";
    }

}
