package com.monst.bankingplugin.config.values;

public class PlayerBankAccountLimit extends OverridableValue<Integer, Integer> implements NativeInteger {

    public PlayerBankAccountLimit() {
        super("player-bank-account-limit", 1);
    }

}
