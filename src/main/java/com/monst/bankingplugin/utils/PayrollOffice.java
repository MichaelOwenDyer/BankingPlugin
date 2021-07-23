package com.monst.bankingplugin.utils;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.lang.*;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.OfflinePlayer;

import java.math.BigDecimal;

public class PayrollOffice {

    private static final Economy ECONOMY = BankingPlugin.getInstance().getEconomy();

    public static boolean allowPayment(OfflinePlayer player, BigDecimal amount) {
        if (amount == null)
            throw new IllegalArgumentException();
        return allowPayment(player, amount.doubleValue());
    }

    public static boolean allowPayment(OfflinePlayer player, double amount) {
        if (player == null)
            throw new IllegalArgumentException();
        if (amount >= 0)
            return true;
        // Return false if withdrawal would bring balance below 0, true if not
        return ECONOMY.getBalance(player) + amount >= 0;
    }

    public static boolean withdraw(OfflinePlayer player, BigDecimal amount) {
        if (amount == null)
            throw new IllegalArgumentException();
        return deposit(player, amount.negate().doubleValue());
    }

    public static boolean withdraw(OfflinePlayer player, double amount) {
        return deposit(player, amount * -1);
    }

    public static boolean deposit(OfflinePlayer player, BigDecimal amount) {
        if (amount == null)
            throw new IllegalArgumentException();
        return deposit(player, amount.doubleValue());
    }

    public static boolean deposit(OfflinePlayer player, double amount) {
        if (player == null)
            throw new IllegalArgumentException();
        if (amount == 0)
            return true;
        Transactor transactor = amount > 0 ? Economy::depositPlayer : Economy::withdrawPlayer;
        EconomyResponse result = transactor.transact(ECONOMY, player, Math.abs(amount));
        if (result.transactionSuccess())
            return true;
        Mailman.notify(player, Messages.get(Message.ERROR_OCCURRED,
                new Replacement(Placeholder.ERROR, result.errorMessage)
        ));
        return false;
    }

    private interface Transactor {
        EconomyResponse transact(Economy economy, OfflinePlayer player, double amount);
    }

}
