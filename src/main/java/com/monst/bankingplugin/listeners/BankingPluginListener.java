package com.monst.bankingplugin.listeners;

import com.monst.bankingplugin.BankingPlugin;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

public abstract class BankingPluginListener implements Listener {

    protected final BankingPlugin plugin;

    protected BankingPluginListener(BankingPlugin plugin) {
        this.plugin = plugin;
    }

    public final void register() {
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
    }

}
