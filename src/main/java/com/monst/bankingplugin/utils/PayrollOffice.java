package com.monst.bankingplugin.utils;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.OfflinePlayer;

public class PayrollOffice {

    private static final Economy ECONOMY = BankingPlugin.getInstance().getEconomy();

    public static boolean allowPayment(OfflinePlayer player, double amount) {
        if (amount >= 0)
            return true;
        return ECONOMY.getBalance(player) >= amount;
    }

    public static boolean withdraw(OfflinePlayer player, double amount) {
        return deposit(player, amount * -1);
    }

    public static boolean deposit(OfflinePlayer player, double amount) {
        if (amount == 0)
            return true;
        Transactor transactor = amount > 0 ? Economy::depositPlayer : Economy::withdrawPlayer;
        EconomyResponse result = transactor.transact(ECONOMY, player, Math.abs(amount));
        if (result.transactionSuccess())
            return true;
        Utils.notify(player, Message.ERROR_OCCURRED.with(Placeholder.ERROR).as(result.errorMessage).translate());
        return false;
    }

    private interface Transactor {
        EconomyResponse transact(Economy economy, OfflinePlayer player, double amount);
    }

}
