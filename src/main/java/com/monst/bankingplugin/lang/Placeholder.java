package com.monst.bankingplugin.lang;

public enum Placeholder {

    OWNER("%OWNER%"),
    BALANCE("%BALANCE%", true),
    ITEM_NAME("%ITEMNAME%"),
    CREATION_PRICE("%CREATION-PRICE%", true),
    ERROR("%ERROR%"),
    MIN_BALANCE("%MIN-BALANCE%", true),
    MAX_BALANCE("%MAX-BALANCE%", true),
    BUY_PRICE("%BUY-PRICE%", true),
    SELL_PRICE("%SELL-PRICE%", true),
    LIMIT("%LIMIT%"),
    PLAYER("%PLAYER%"),
    PROPERTY("%PROPERTY%"),
    VALUE("%VALUE%"),
    EXTENDED("%EXTENDED%"),
    REVENUE("%REVENUE%", true),
    GENERATION("%GENERATION%"),
    STOCK("%STOCK%"),
    CHEST_SPACE("%CHEST-SPACE%"),
    MAX_STACK("%MAX-STACK%"),
    COMMAND("%COMMAND%"),
    VERSION("%VERSION%");

    private final String name;
    private final boolean isMoney;

    Placeholder(String name) {
        this(name, false);
    }

    Placeholder(String name, boolean isMoney) {
        this.name = name;
        this.isMoney = isMoney;
    }

    public boolean isMoney() {
        return isMoney;
    }

    @Override
    public String toString() {
        return name;
    }

}
