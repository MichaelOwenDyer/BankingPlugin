package com.monst.bankingplugin.configuration.values;

import com.monst.bankingplugin.entity.Bank;
import com.monst.pluginconfiguration.exception.ArgumentParseException;

public interface BankPolicy<T> {

    T at(Bank bank);

    boolean parseAndSetAt(Bank bank, String input) throws ArgumentParseException;

    String toStringAt(Bank bank);

    static String defaultPath(String path) {
        return path + ".default";
    }

    static String allowOverridePath(String path) {
        return path + ".allow-override";
    }

}
