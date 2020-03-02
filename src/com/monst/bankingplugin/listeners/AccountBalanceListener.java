package com.monst.bankingplugin.listeners;

import java.math.BigDecimal;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;

import com.monst.bankingplugin.Account;
import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.utils.AccountUtils;
import com.monst.bankingplugin.utils.Messages;
import com.monst.bankingplugin.utils.Permissions;

public class AccountBalanceListener implements Listener {
	
	private BankingPlugin plugin;
	private AccountUtils accountUtils;

	public AccountBalanceListener(BankingPlugin plugin) {
		this.plugin = plugin;
		this.accountUtils = plugin.getAccountUtils();
	}

	// TODO: Move this somewhere else? Block unwanted account views
	@EventHandler
	public void onAccountInventoryOpen(InventoryOpenEvent e) {
		
		if (!(e.getPlayer() instanceof Player) || e.getInventory() == null)
			return;
		if (!e.getInventory().getType().equals(InventoryType.CHEST))
			return;
		
		Location loc = e.getInventory().getLocation();
		
		if (accountUtils.isAccount(loc)) {
			Player player = Bukkit.getPlayer(e.getPlayer().getUniqueId());
			Account account = accountUtils.getAccount(loc);
			if (!player.getUniqueId().equals(account.getOwner().getUniqueId())
					&& !player.hasPermission(Permissions.ACCOUNT_VIEW_OTHER)) {
				player.sendMessage(Messages.NO_PERMISSION_ACCOUNT_VIEW_OTHER);
				e.setCancelled(true);
			}
		}
	}

	// TODO: Add configuration for sending transaction messages to executor /
	// player?
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

			BigDecimal difference = valueOnClose.subtract(account.getBalance());
			if (difference.compareTo(BigDecimal.ZERO) == 0)
				return;

			Player executor = (Player) e.getPlayer();
			account.getStatus().setBalance(valueOnClose);

			executor.sendMessage(getTransactionMessage(executor, account, difference));
			executor.sendMessage(getNewBalanceMessage(executor, account, difference));
		}
	}

	private String getTransactionMessage(Player executor, Account account, BigDecimal difference) {
		
		StringBuilder sb = new StringBuilder("You have ");
		
		boolean transactionType = difference.signum() == 1;
		if (!transactionType)
			difference.abs();

		if (transactionType)
			sb.append("deposited $" + difference.toString() + " into ");
		else
			sb.append("withdrawn $" + difference.toString() + " from ");
		
		if (executor.getUniqueId().equals(account.getOwner().getUniqueId()))
			sb.append("your account.");
		else {
			sb.append(account.getOwner().getName() + "'s account.");
		}
		return sb.toString();
	}

	private String getNewBalanceMessage(Player executor, Account account, BigDecimal balance) {
		if (executor.getUniqueId().equals(account.getOwner().getUniqueId()))
			return "Your new balance is $" + balance.toString() + ".";
		else
			return account.getOwner().getName() + "'s new balance is $" + balance.toString() + ".";
	}
}

