package com.monst.bankingplugin.utils;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.OfflinePlayer;

import java.math.BigDecimal;

public class PayrollOffice {

    private static final Economy ECONOMY = BankingPlugin.getInstance().getEconomy();

    public static boolean allowPayment(OfflinePlayer player, BigDecimal amount) {
        if (amount.signum() >= 0)
            return true;
        return BigDecimal.valueOf(ECONOMY.getBalance(player)).compareTo(amount) >= 0;
    }

    public static boolean withdraw(OfflinePlayer player, BigDecimal amount) {
        return deposit(player, amount.negate());
    }

    public static boolean deposit(OfflinePlayer player, BigDecimal amount) {
        if (amount.signum() == 0)
            return true;
        EconomyResponse response;
        if (amount.signum() > 0)
            response = ECONOMY.depositPlayer(player, amount.doubleValue());
        else
            response = ECONOMY.withdrawPlayer(player, amount.abs().doubleValue());
        if (response.transactionSuccess())
            return true;
        Utils.notify(player, Message.ERROR_OCCURRED.with(Placeholder.ERROR).as(response.errorMessage).translate());
        return false;
    }

}
