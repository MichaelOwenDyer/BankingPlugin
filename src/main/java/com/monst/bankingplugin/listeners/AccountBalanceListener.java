package com.monst.bankingplugin.listeners;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.banking.account.Account;
import com.monst.bankingplugin.banking.account.AccountField;
import com.monst.bankingplugin.events.account.AccountTransactionEvent;
import com.monst.bankingplugin.lang.LangUtils;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import com.monst.bankingplugin.lang.Replacement;
import com.monst.bankingplugin.sql.logging.AccountTransaction;
import com.monst.bankingplugin.utils.Callback;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.math.BigDecimal;

/**
 * Continuously updates account balances.
 * @see Account#calculateBalance()
 */
public class AccountBalanceListener extends BankingPluginListener {

	public AccountBalanceListener(BankingPlugin plugin) {
		super(plugin);
	}

	@EventHandler
	@SuppressWarnings("unused")
	public void onAccountInventoryClose(InventoryCloseEvent e) {
		if (!(e.getPlayer() instanceof Player))
			return;
		Player executor = (Player) e.getPlayer();

		Location loc = e.getInventory().getLocation();
		if (loc == null)
			return;
		Block b = loc.getBlock();
		if (b.getType() != Material.CHEST && b.getType() != Material.TRAPPED_CHEST)
			return;

		Account account = accountRepo.getAt(b.getLocation());
		if (account == null)
			return;

		evaluateAccountTransaction(executor, account);
	}

	private void evaluateAccountTransaction(Player executor, Account account) {

		BigDecimal valueOnClose = account.calculateBalance();
		BigDecimal balance = account.getBalance();
		BigDecimal difference = valueOnClose.subtract(balance);

		if (difference.signum() == 0)
			return;

		plugin.debugf("Appraised account balance: %s, diff: %s (#%d)",
				Utils.format(valueOnClose), Utils.format(difference), account.getID());

		executor.sendMessage(LangUtils.getMessage(difference.signum() > 0 ? Message.ACCOUNT_DEPOSIT : Message.ACCOUNT_WITHDRAWAL,
				new Replacement(Placeholder.AMOUNT, difference::abs),
				new Replacement(Placeholder.ACCOUNT_BALANCE, valueOnClose)
		));

		if (difference.signum() < 0 && valueOnClose.compareTo(account.getPrevBalance()) < 0)
			if (account.getMultiplierStage() != account.processWithdrawal())
				executor.sendMessage(LangUtils.getMessage(Message.MULTIPLIER_DECREASED,
						new Replacement(Placeholder.NUMBER, account::getRealMultiplier)
				));

		account.setBalance(valueOnClose);
		accountRepo.update(account, Callback.blank(), AccountField.BALANCE);

		plugin.debugf("Account #%d has been updated with a new balance (%s)", account.getID(), Utils.format(valueOnClose));
		new AccountTransactionEvent(executor, account, difference, valueOnClose).fire();

		if (account.getOwner().isOnline())
			plugin.getDatabase().logLastSeen(account.getOwner().getPlayer());

		plugin.getDatabase().logAccountTransaction(new AccountTransaction(
				account.getID(), account.getBank().getID(), executor.getUniqueId(), executor.getName(),
				account.getBalance(), balance, difference, System.currentTimeMillis()
		));
	}

}

