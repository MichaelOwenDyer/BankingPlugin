package com.monst.bankingplugin.configuration.values;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.configuration.ConfigurationValue;
import com.monst.bankingplugin.configuration.transform.PatternTransformer;

import java.util.Optional;
import java.util.regex.Pattern;

public class NameRegex extends ConfigurationValue<Optional<Pattern>> {

    public NameRegex(BankingPlugin plugin) {
        super(plugin, "name-regex", Optional.empty(), new PatternTransformer().optional());
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean allows(String name) {
        return get().map(pattern -> pattern.matcher(name).matches()).orElse(true);
    }

}
