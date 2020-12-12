package com.monst.bankingplugin.listeners;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.utils.AccountRepository;
import org.bukkit.event.Listener;

public class BankingPluginListener implements Listener {

    protected final BankingPlugin plugin;
    protected final AccountRepository accountRepo;

    protected BankingPluginListener(BankingPlugin plugin) {
        this.plugin = plugin;
        this.accountRepo = plugin.getAccountRepository();
    }

}
