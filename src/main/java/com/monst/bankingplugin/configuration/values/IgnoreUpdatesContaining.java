package com.monst.bankingplugin.configuration.values;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.configuration.ConfigurationValue;
import com.monst.bankingplugin.configuration.transform.StringTransformer;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class IgnoreUpdatesContaining extends ConfigurationValue<Set<String>> {

    public IgnoreUpdatesContaining(BankingPlugin plugin) {
        super(plugin, "ignore-updates-containing", Collections.emptySet(),
                new StringTransformer().collect(HashSet::new));
    }

    public boolean ignore(String version) {
        return get().stream().anyMatch(version::contains);
    }

}
