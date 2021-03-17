package com.monst.bankingplugin.listeners;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.banking.account.Account;
import com.monst.bankingplugin.banking.account.AccountField;
import com.monst.bankingplugin.sql.logging.AccountTransactionReceipt;
import com.monst.bankingplugin.events.account.AccountTransactionEvent;
import com.monst.bankingplugin.exceptions.AccountNotFoundException;
import com.monst.bankingplugin.geo.locations.ChestLocation;
import com.monst.bankingplugin.lang.LangUtils;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import com.monst.bankingplugin.lang.Replacement;
import com.monst.bankingplugin.utils.Callback;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;

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
		if (e.getInventory().getType() != InventoryType.CHEST)
			return;

		Chest chest = Utils.getChestHolding(e.getInventory());
		if (chest == null)
			return;
		ChestLocation loc = ChestLocation.from(chest);

		Account account;
		try {
			account = accountRepo.getAt(loc);
		} catch (AccountNotFoundException ex) {
			return;
		}

		BigDecimal valueOnClose = account.calculateBalance();
		BigDecimal balance = account.getBalance();
		BigDecimal difference = valueOnClose.subtract(balance);

		if (difference.signum() == 0)
			return;

		plugin.debugf("Appraised account balance: %s, diff: %s (#%d)",
				Utils.format(valueOnClose), Utils.format(difference), account.getID());

		Player executor = (Player) e.getPlayer();
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
		Bukkit.getPluginManager().callEvent(new AccountTransactionEvent(executor, account, difference, valueOnClose));

		if (account.getOwner().isOnline())
			plugin.getDatabase().logLastSeen(account.getOwner().getPlayer());

		plugin.getDatabase().logAccountTransaction(new AccountTransactionReceipt(
				account.getID(), account.getBank().getID(), executor.getUniqueId(), executor.getName(),
				account.getBalance(), balance, difference, System.currentTimeMillis()
		));
	}
}

