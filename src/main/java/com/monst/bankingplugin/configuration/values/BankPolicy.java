package com.monst.bankingplugin.configuration.values;

import com.monst.bankingplugin.configuration.exception.ArgumentParseException;
import com.monst.bankingplugin.entity.Bank;

/**
 * Represents a configuration policy that a bank may or may not set.
 * If a bank sets a particular policy, it overrides the global policy in the configuration.
 * @param <T>
 */
public interface BankPolicy<T> {

    T at(Bank bank);

    boolean parseAndSetAt(Bank bank, String input) throws ArgumentParseException;

    String toStringAt(Bank bank);
    
    AllowOverride getAllowOverride();

    static String defaultPath(String path) {
        return path + ".default";
    }

    static String allowOverridePath(String path) {
        return path + ".allow-override";
    }

}
