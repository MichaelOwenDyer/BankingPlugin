package com.monst.bankingplugin.config.values;

import com.monst.bankingplugin.BankingPlugin;
import org.bukkit.command.CommandSender;

public class EnableDebugLog extends ConfigValue<Boolean, Boolean> implements NativeBoolean {

    public EnableDebugLog(BankingPlugin plugin) {
        super(plugin, "enable-debug-log", false);
    }

    @Override
    void beforeSet() {
        if (!get())
            plugin.debug("Debug log disabled.");
    }

    @Override
    public void afterSet(CommandSender executor) {
        if (get())
            plugin.debug("Debug log enabled.");
    }

}
