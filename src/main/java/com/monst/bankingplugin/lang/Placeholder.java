package com.monst.bankingplugin.lang;

public enum Placeholder {

    PRICE,
    AMOUNT,
    AMOUNT_REMAINING,
    PLAYER_BALANCE,
    ACCOUNT_BALANCE,
    ACCOUNT_ID,
    BANK_NAME,
    NAME,
    PLAYER,
    NUMBER_OF_ACCOUNTS,
    NUMBER_OF_BANKS,
    BANK_SIZE,
    MINIMUM,
    MAXIMUM,
    DIFFERENCE,
    INTEREST_MULTIPLIER,
    INPUT,
    LIMIT,
    PROPERTY,
    POLICY,
    VALUE,
    PREVIOUS_VALUE,
    PATTERN,
    COMMAND,
    VERSION,
    URL;

    @Override
    public String toString() {
        return "%" + name() + "%";
    }

}
