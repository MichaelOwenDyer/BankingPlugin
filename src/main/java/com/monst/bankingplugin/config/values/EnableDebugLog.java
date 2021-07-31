package com.monst.bankingplugin.config.values;

import org.bukkit.command.CommandSender;

public class EnableDebugLog extends ConfigValue<Boolean, Boolean> implements NativeBoolean {

    public EnableDebugLog() {
        super("enable-debug-log", false);
    }

    @Override
    void beforeSet() {
        if (!get())
            PLUGIN.debug("Debug log disabled.");
    }

    @Override
    public void afterSet(CommandSender executor) {
        if (get())
            PLUGIN.debug("Debug log enabled.");
    }

}
