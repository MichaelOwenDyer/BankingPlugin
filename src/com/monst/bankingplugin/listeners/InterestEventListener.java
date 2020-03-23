package com.monst.bankingplugin.listeners;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.monst.bankingplugin.Account;
import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.events.interest.InterestEvent;
import com.monst.bankingplugin.utils.AccountStatus;
import com.monst.bankingplugin.utils.AccountUtils;
import com.monst.bankingplugin.utils.Messages;
import com.monst.bankingplugin.utils.Utils;

import net.milkbowl.vault.economy.EconomyResponse;

public class InterestEventListener implements Listener {
	
	private BankingPlugin plugin;
	private AccountUtils accountUtils;
	
	public InterestEventListener(BankingPlugin plugin) {
		this.plugin = plugin;
		this.accountUtils = plugin.getAccountUtils();
	}

	@EventHandler
	public void onInterestEvent(InterestEvent e) {
		
		plugin.debug("Interest payout event occurring now!");
		
		Map<OfflinePlayer, List<Account>> playerAccounts = accountUtils.getAccountsCopy().stream()
				.collect(Collectors.groupingBy(Account::getOwner));

		for (OfflinePlayer owner : playerAccounts.keySet()) {
			
			World world = playerAccounts.get(owner).get(0).getLocation().getWorld();

			BigDecimal sumInterest = BigDecimal.ZERO;

			for (Account account : playerAccounts.get(owner)) {

				AccountStatus status = account.getStatus();
				if (!status.allowNextPayout(owner.isOnline())) {
					plugin.getDatabase().addAccount(account, null);
					continue;
				}

				BigDecimal baseInterest = account.getBalance().multiply(BigDecimal.valueOf(Config.baselineInterestRate))
						.setScale(2, RoundingMode.HALF_EVEN);
				BigDecimal interest = baseInterest;

				int multiplier = status.getRealMultiplier();
				if (Config.enableInterestMultipliers) {
					interest = interest.multiply(BigDecimal.valueOf(multiplier));
				}

				status.incrementMultiplier(owner.isOnline());
				sumInterest = sumInterest.add(interest);
				account.getStatus().updatePrevBalance();

				plugin.getDatabase().addAccount(account, null);

				if (Config.enableInterestLog) {
					plugin.getDatabase().logInterest(account, baseInterest, multiplier, interest, null);
				}
			}

			EconomyResponse r = plugin.getEconomy().depositPlayer(owner, world.getName(), sumInterest.doubleValue());
			if (!r.transactionSuccess()) {
				plugin.debug("Economy transaction failed: " + r.errorMessage);
				owner.getPlayer().sendMessage(Messages.ERROR_OCCURRED);
			} else
				owner.getPlayer().sendMessage(Messages.getWithValue(Messages.INTEREST_EARNED, Utils.formatNumber(sumInterest)));

			if (owner.isOnline())
				plugin.getDatabase().logLogout(owner.getPlayer(), null);
		}
	}
}
