package com.monst.bankingplugin.configuration.values;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.configuration.exception.ArgumentParseException;
import com.monst.bankingplugin.configuration.type.IntegerConfigurationValue;
import com.monst.bankingplugin.configuration.validation.Bound;
import com.monst.bankingplugin.entity.Bank;

import java.util.Optional;

public class PlayerBankAccountLimit extends IntegerConfigurationValue implements BankPolicy<Integer> {

    private final AllowOverride allowOverride;

    public PlayerBankAccountLimit(BankingPlugin plugin) {
        super(plugin, BankPolicy.defaultPath("player-bank-account-limit"), 1);
        this.allowOverride = new AllowOverride(plugin, "player-bank-account-limit");
    }

    @Override
    protected Bound<Integer> getBound() {
        return Bound.atLeast(-1);
    }

    @Override
    public Integer at(Bank bank) {
        if (bank.getPlayerBankAccountLimit() == null) {
            if (plugin.config().stickyDefaults.get())
                bank.setPlayerBankAccountLimit(get());
            return get();
        }
        return allowOverride.get() ? bank.getPlayerBankAccountLimit() : get();
    }

    @Override
    public boolean parseAndSetAt(Bank bank, String input) throws ArgumentParseException {
        if (input == null || input.isEmpty()) {
            bank.setPlayerBankAccountLimit(plugin.config().stickyDefaults.get() ? get() : null);
            return true;
        }
        bank.setPlayerBankAccountLimit(parse(input));
        return allowOverride.get();
    }

    @Override
    public String toStringAt(Bank bank) {
        return format(Optional.ofNullable(bank.getPlayerBankAccountLimit()).orElseGet(this));
    }
    
    @Override
    public AllowOverride getAllowOverride() {
        return allowOverride;
    }
    
}
