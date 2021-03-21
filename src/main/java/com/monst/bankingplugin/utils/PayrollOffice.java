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
        if (player == null || amount == null)
            throw new IllegalArgumentException();
        if (amount.signum() >= 0)
            return true;
        // Return false if withdrawal would bring balance below 0, true if not
        return BigDecimal.valueOf(ECONOMY.getBalance(player)).add(amount).signum() >= 0;
    }

    public static boolean submit(OfflinePlayer player, BigDecimal amount) {
        if (player == null || amount == null)
            throw new IllegalArgumentException();
        if (amount.signum() == 0)
            return true;
        Transactor transactor = amount.signum() > 0 ? Economy::depositPlayer : Economy::withdrawPlayer;
        EconomyResponse result = transactor.transact(ECONOMY, player, amount.abs().doubleValue());
        if (result.transactionSuccess())
            return true;
        Mailman.notify(player, LangUtils.getMessage(Message.ERROR_OCCURRED,
                new Replacement(Placeholder.ERROR, result.errorMessage)
        ));
        return false;
    }

    private interface Transactor {
        EconomyResponse transact(Economy economy, OfflinePlayer player, double amount);
    }

}
