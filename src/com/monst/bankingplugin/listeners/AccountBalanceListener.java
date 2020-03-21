package com.monst.bankingplugin.listeners;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;

import com.monst.bankingplugin.Account;
import com.monst.bankingplugin.Account.TransactionType;
import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.utils.AccountUtils;
import com.monst.bankingplugin.utils.Utils;

public class AccountBalanceListener implements Listener {
	
	private BankingPlugin plugin;
	private AccountUtils accountUtils;

	public AccountBalanceListener(BankingPlugin plugin) {
		this.plugin = plugin;
		this.accountUtils = plugin.getAccountUtils();
	}

	@EventHandler
	public void onAccountInventoryClose(InventoryCloseEvent e) {

		if (!(e.getPlayer() instanceof Player) || e.getInventory() == null)
			return;
		if (!e.getInventory().getType().equals(InventoryType.CHEST))
			return;

		Location loc = e.getInventory().getLocation();

		if (accountUtils.isAccount(loc)) {
			Account account = accountUtils.getAccount(loc);
			BigDecimal valueOnClose = accountUtils.appraiseAccountContents(account);
			valueOnClose = valueOnClose.setScale(2, RoundingMode.HALF_EVEN);

			BigDecimal difference = valueOnClose.subtract(account.getBalance());
			if (difference.signum() == 0)
				return;

			Player executor = (Player) e.getPlayer();
			account.getStatus().setBalance(valueOnClose);

			executor.sendMessage(getTransactionMessage(executor, account, difference));
			executor.sendMessage(getNewBalanceMessage(executor, account, valueOnClose));

			if (difference.signum() == -1) {
				int multiplier = account.getStatus().getMultiplierStage();
				if (multiplier != account.getStatus().processWithdrawal())
					executor.sendMessage(ChatColor.GOLD + "Your multiplier has decreased to " + ChatColor.GREEN
							+ account.getStatus().getRealMultiplier() + ChatColor.GOLD + "!");
			}

			plugin.getDatabase().addAccount(account, null);

			if (Config.enableTransactionLog) {
				TransactionType type = difference.signum() == 1 ? TransactionType.DEPOSIT : TransactionType.WITHDRAWAL;
				plugin.getDatabase().logTransaction(executor, account, difference.abs(), type, null);
				plugin.debug("Logging transaction to database...");
			}
		}
	}

	private String getTransactionMessage(Player executor, Account account, BigDecimal difference) {
		
		StringBuilder sb = new StringBuilder("You have ");
		
		boolean transactionType = difference.signum() == 1;
		if (!transactionType)
			difference.abs();

		if (transactionType)
			sb.append("deposited " + ChatColor.GREEN + "$" + Utils.formatNumber(difference) + ChatColor.WHITE + " into ");
		else
			sb.append("withdrawn " + ChatColor.RED + "$" + Utils.formatNumber(difference.abs()) + ChatColor.WHITE + " from ");
		
		if (account.isOwner(executor))
			sb.append("your account.");
		else {
			sb.append(account.getOwner().getName() + "'s account.");
		}
		return sb.toString();
	}

	private String getNewBalanceMessage(Player executor, Account account, BigDecimal balance) {
		if (account.isOwner(executor))
			return "Your new balance is " + ChatColor.GREEN + "$" + Utils.formatNumber(balance) + ChatColor.WHITE + ".";
		else
			return account.getOwner().getName() + "'s new balance is $" + Utils.formatNumber(balance) + ".";
	}
}

