package com.monst.bankingplugin.config.values;

import com.monst.bankingplugin.BankingPlugin;

public class PlayerBankAccountLimit extends OverridableValue<Integer, Integer> implements NativeInteger {

    public PlayerBankAccountLimit(BankingPlugin plugin) {
        super(plugin, "player-bank-account-limit", 1);
    }

}
