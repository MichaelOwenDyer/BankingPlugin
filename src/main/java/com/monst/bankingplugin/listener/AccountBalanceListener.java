package com.monst.bankingplugin.listener;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.entity.Account;
import com.monst.bankingplugin.entity.Bank;
import com.monst.bankingplugin.entity.log.AccountTransaction;
import com.monst.bankingplugin.event.account.AccountContractEvent;
import com.monst.bankingplugin.event.account.AccountTransactionEvent;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

import java.math.BigDecimal;

/**
 * Continuously updates account balances.
 */
public class AccountBalanceListener implements Listener {

	private final BankingPlugin plugin;

	public AccountBalanceListener(BankingPlugin plugin) {
		this.plugin = plugin;
	}

	@EventHandler(ignoreCancelled = true)
	public void onAccountContract(AccountContractEvent e) {
		// Some items may have spilled out when the chest was broken.
		evaluateAccountTransaction(e.getPlayer(), e.getAccount());
	}

	@EventHandler
	public void onAccountInventoryClose(InventoryCloseEvent e) {
		Inventory inv = e.getInventory();
		if (inv.getType() != InventoryType.CHEST)
			return;
		Location loc = inv.getLocation();
		if (loc == null)
			return;
		// The inventory was of a chest
		Account account = plugin.getAccountService().findAtChest(loc.getBlock());
		if (account == null)
			return;
		// The chest was an account
		// Evaluate if a transaction occurred
		evaluateAccountTransaction((Player) e.getPlayer(), account);
	}

	private void evaluateAccountTransaction(Player executor, Account account) {

		BigDecimal oldBalance = account.getBalance();
		BigDecimal newBalance = plugin.getWorths().appraise(account);
		BigDecimal difference = newBalance.subtract(oldBalance);

		if (difference.signum() == 0)
			return;
		
		account.setBalance(newBalance);

		plugin.debugf("Appraised balance of account #d: %s, difference to previous: %s",
				account.getID(), newBalance, difference);

		Message message = difference.signum() > 0 ? Message.ACCOUNT_DEPOSIT : Message.ACCOUNT_WITHDRAWAL;
		executor.sendMessage(message
				.with(Placeholder.AMOUNT).as(plugin.getEconomy().format(difference.abs().doubleValue()))
				.and(Placeholder.ACCOUNT_BALANCE).as(plugin.getEconomy().format(newBalance.doubleValue()))
				.translate(plugin));

		if (difference.signum() < 0 && newBalance.compareTo(account.getPreviousBalance()) < 0) {
			Bank bank = account.getBank();
			int multiplierStage = account.getInterestMultiplierStage();
			account.decrementMultiplier(plugin.config().withdrawalMultiplierDecrement.at(bank));
			if (multiplierStage != account.getInterestMultiplierStage())
				executor.sendMessage(Message.ACCOUNT_INTEREST_MULTIPLIER_DECREASED
						.with(Placeholder.INTEREST_MULTIPLIER).as(account.getInterestMultiplier(plugin.config().interestMultipliers.at(bank)))
						.translate(plugin));
		}

		plugin.getAccountService().update(account);
		plugin.debugf("Account #%d has been updated with a new balance of %s", account.getID(), newBalance);
		new AccountTransactionEvent(executor, account, difference, newBalance).fire();

		if (account.getOwner().isOnline())
			plugin.getLastSeenService().updateLastSeenTime(account.getOwner());

		plugin.getAccountTransactionService().save(
				new AccountTransaction(account, account.getBank(), executor, oldBalance, difference, newBalance));
	}

}

