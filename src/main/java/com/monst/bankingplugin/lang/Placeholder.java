package com.monst.bankingplugin.lang;

import com.monst.bankingplugin.BankingPlugin;

public enum Placeholder {

    AMOUNT ("%AMOUNT%", true),
    BANK_NAME ("%BANK_NAME%"),
    PLAYER ("%PLAYER%"),
    OWNER ("%OWNER%"),
    NUMBER_OF_ACCOUNTS ("%NUMBER_OF_ACCOUNTS%"),
    NUMBER_OF_BANKS ("%NUMBER_OF_BANKS%"),
    NUMBER ("%NUMBER%"),
    STRING ("%STRING%"),
    ACCOUNT_BALANCE ("%ACCOUNT_BALANCE%", true),
    MULTIPLIER ("%MULTIPLIER%"),
    ERROR ("%ERROR%"),
    LIMIT ("%LIMIT%"),
    PROPERTY ("%PROPERTY%"),
    VALUE ("%VALUE%"),
    COMMAND ("%COMMAND%"),
    VERSION ("%VERSION%");

    private final String name;
    private final boolean isMoney;

    Placeholder(String name) {
        this(name, false);
    }

    Placeholder(String name, boolean isMoney) {
        this.name = name;
        this.isMoney = isMoney;
    }

    @Override
    public String toString() {
        if (isMoney)
            return BankingPlugin.getInstance().getEconomy().format(Double.parseDouble(name));
        return name;
    }

}
