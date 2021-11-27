package com.monst.bankingplugin.listeners;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.banking.Account;
import com.monst.bankingplugin.banking.AccountField;
import com.monst.bankingplugin.events.account.AccountContractEvent;
import com.monst.bankingplugin.events.account.AccountTransactionEvent;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import com.monst.bankingplugin.repository.AccountRepository;
import com.monst.bankingplugin.sql.logging.AccountTransaction;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.math.BigDecimal;

/**
 * Continuously updates account balances.
 * @see Account#calculateBalance()
 */
public class AccountBalanceListener extends BankingPluginListener {

	private final AccountRepository accountRepo;

	public AccountBalanceListener(BankingPlugin plugin) {
		super(plugin);
		this.accountRepo = plugin.getAccountRepository();
	}

	@EventHandler(ignoreCancelled = true)
	public void onAccountContract(AccountContractEvent e) {
		evaluateAccountTransaction(e.getPlayer(), e.getAccount());
	}

	@EventHandler
	public void onAccountInventoryClose(InventoryCloseEvent e) {
		if (!(e.getPlayer() instanceof Player))
			return;
		Player executor = (Player) e.getPlayer();

		Location loc = e.getInventory().getLocation();
		if (loc == null)
			return;
		Account account = accountRepo.getAt(loc.getBlock());
		if (account == null)
			return;

		evaluateAccountTransaction(executor, account);
	}

	private void evaluateAccountTransaction(Player executor, Account account) {

		BigDecimal oldBalance = account.getBalance();
		account.reloadBalance();
		BigDecimal newBalance = account.getBalance();
		BigDecimal difference = newBalance.subtract(oldBalance);

		if (difference.signum() == 0)
			return;

		plugin.debugf("Appraised balance of account #d: %s, difference to previous: %s", account.getID(),
				Utils.format(newBalance), Utils.format(difference));

		Message message = difference.signum() > 0 ? Message.ACCOUNT_DEPOSIT : Message.ACCOUNT_WITHDRAWAL;
		executor.sendMessage(message
				.with(Placeholder.AMOUNT).as(difference.abs())
				.and(Placeholder.ACCOUNT_BALANCE).as(newBalance)
				.translate());

		if (difference.signum() < 0 && newBalance.compareTo(account.getPreviousBalance()) < 0)
			if (account.getMultiplierStage() != account.processWithdrawal())
				executor.sendMessage(Message.MULTIPLIER_DECREASED.with(Placeholder.NUMBER).as(account.getRealMultiplier()).translate());

		accountRepo.update(account, AccountField.BALANCE);

		plugin.debugf("Account #%d has been updated and a new balance of %s", account.getID(), Utils.format(newBalance));
		new AccountTransactionEvent(executor, account, difference, newBalance).fire();

		if (account.getOwner().isOnline())
			plugin.getDatabase().logLastSeen(account.getOwner().getPlayer());

		plugin.getDatabase().logAccountTransaction(new AccountTransaction(
				account.getID(), account.getBank().getID(), executor.getUniqueId(), executor.getName(),
				newBalance, oldBalance, difference, System.currentTimeMillis()
		));
	}

}

