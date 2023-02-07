package com.monst.bankingplugin.util;

import com.monst.bankingplugin.BankingPlugin;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.OfflinePlayer;

public class PaymentService {

    private final BankingPlugin plugin;

    public PaymentService(BankingPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean deposit(OfflinePlayer player, double amount) {
        if (amount < 0)
            throw new IllegalArgumentException("Cannot deposit negative amount");
        return transact(player, amount);
    }

    public boolean withdraw(OfflinePlayer player, double amount) {
        if (amount < 0)
            throw new IllegalArgumentException("Cannot withdraw negative amount");
        return transact(player, amount * -1);
    }

    public boolean transact(OfflinePlayer player, double amount) {
        if (amount == 0)
            return true;
        EconomyResponse response;
        if (amount > 0)
            response = plugin.getEconomy().depositPlayer(player, amount);
        else
            response = plugin.getEconomy().withdrawPlayer(player, amount * -1);
        switch (response.type) {
            case SUCCESS:
                return true;
            case NOT_IMPLEMENTED:
                plugin.debug(new IllegalStateException(response.errorMessage));
            case FAILURE:
                return false;
            default:
                throw new IllegalStateException("Unknown EconomyResponse type: " + response.type);
        }
    }

}
