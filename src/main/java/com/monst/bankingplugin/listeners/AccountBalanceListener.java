package com.monst.bankingplugin.listeners;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.banking.account.Account;
import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.events.account.AccountTransactionEvent;
import com.monst.bankingplugin.utils.AccountUtils;
import com.monst.bankingplugin.utils.Messages;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;

import java.math.BigDecimal;

/**
 * Continuously updates account balances.
 * @see AccountUtils#appraise(Account)
 */
public class AccountBalanceListener implements Listener {
	
	private final BankingPlugin plugin;
	private final AccountUtils accountUtils;

	public AccountBalanceListener(BankingPlugin plugin) {
		this.plugin = plugin;
		this.accountUtils = plugin.getAccountUtils();
	}

	@EventHandler
	@SuppressWarnings("unused")
	public void onAccountInventoryClose(InventoryCloseEvent e) {

		if (!(e.getPlayer() instanceof Player))
			return;
		if (!e.getInventory().getType().equals(InventoryType.CHEST))
			return;

		Account account = accountUtils.getAccount(e.getInventory().getLocation());
		if (account == null)
			return;

		BigDecimal valueOnClose = AccountUtils.appraise(account);
		BigDecimal difference = valueOnClose.subtract(account.getBalance());

		if (difference.signum() == 0)
			return;

		Player executor = (Player) e.getPlayer();
		BigDecimal abs = difference.abs();
		executor.sendMessage(String.format(difference.signum() > 0 ? Messages.ACCOUNT_DEPOSIT : Messages.ACCOUNT_WITHDRAWAL,
				Utils.format(difference), (account.isOwner(executor)) ? "your" : account.getOwner().getName() + "'s"));
		executor.sendMessage(String.format(Messages.ACCOUNT_NEW_BALANCE, Utils.format(valueOnClose)));

		if (difference.signum() < 0 && valueOnClose.compareTo(account.getPrevBalance()) < 0)
			if (account.getStatus().getMultiplierStage() != account.getStatus().processWithdrawal())
				executor.sendMessage(String.format(Messages.MULTIPLIER_DECREASED, account.getStatus().getRealMultiplier()));

		account.setBalance(valueOnClose);
		accountUtils.addAccount(account, true);

		plugin.debugf("Account #%d has been updated with a new balance ($%s)", account.getID(), Utils.format(valueOnClose));
		Bukkit.getPluginManager().callEvent(new AccountTransactionEvent(executor, account, difference, valueOnClose));

		if (account.getOwner().isOnline())
			plugin.getDatabase().logLogout(account.getOwner().getPlayer(), null);

		if (Config.enableTransactionLog) {
			plugin.getDatabase().logAccountTransaction(executor, account, difference, null);
			plugin.debug("Logging transaction to database...");
		}
	}
}

