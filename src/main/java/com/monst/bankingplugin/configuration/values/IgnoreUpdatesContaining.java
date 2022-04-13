package com.monst.bankingplugin.configuration.values;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.pluginconfiguration.ConfigurationCollection;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class IgnoreUpdatesContaining extends ConfigurationCollection<String, Set<String>> {

    public IgnoreUpdatesContaining(BankingPlugin plugin) {
        super(plugin, "ignore-updates-containing", Collections.emptySet());
    }

    @Override
    protected Set<String> createCollection() {
        return new HashSet<>();
    }

    @Override
    protected String parseElement(String input) {
        return input;
    }

    public boolean ignore(String version) {
        return get().stream().anyMatch(version::contains);
    }

}
