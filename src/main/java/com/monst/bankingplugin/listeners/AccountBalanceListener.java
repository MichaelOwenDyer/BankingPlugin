package com.monst.bankingplugin.listeners;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.banking.account.Account;
import com.monst.bankingplugin.events.account.AccountTransactionEvent;
import com.monst.bankingplugin.lang.LangUtils;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import com.monst.bankingplugin.lang.Replacement;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;

import java.math.BigDecimal;

/**
 * Continuously updates account balances.
 * @see Account#calculateValue()
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
		if (!e.getInventory().getType().equals(InventoryType.CHEST))
			return;

		Account account = accountRepo.getAt(e.getInventory().getLocation());
		if (account == null)
			return;

		BigDecimal valueOnClose = account.calculateValue();
		BigDecimal difference = valueOnClose.subtract(account.getBalance());

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
			if (account.getStatus().getMultiplierStage() != account.getStatus().processWithdrawal())
				executor.sendMessage(LangUtils.getMessage(Message.MULTIPLIER_DECREASED,
						new Replacement(Placeholder.NUMBER, () -> account.getStatus().getRealMultiplier())
				));

		account.setBalance(valueOnClose);
		accountRepo.add(account, true);

		plugin.debugf("Account #%d has been updated with a new balance (%s)", account.getID(), Utils.format(valueOnClose));
		Bukkit.getPluginManager().callEvent(new AccountTransactionEvent(executor, account, difference, valueOnClose));

		if (account.getOwner().isOnline())
			plugin.getDatabase().logLastSeen(account.getOwner().getPlayer(), null);

		plugin.getDatabase().logAccountTransaction(executor, account, difference, null);
	}
}

