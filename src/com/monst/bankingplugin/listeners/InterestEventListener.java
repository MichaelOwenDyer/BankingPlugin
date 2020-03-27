package com.monst.bankingplugin.listeners;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
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

		Map<OfflinePlayer, BigDecimal> totalPayouts = new HashMap<>();
		Map<OfflinePlayer, Integer> counter = new HashMap<>();

		Map<OfflinePlayer, List<Account>> playerAccounts = accountUtils.getAccountsCopy().stream()
				.collect(Collectors.groupingBy(Account::getOwner));

		for (OfflinePlayer owner : playerAccounts.keySet()) {
			
			World world = playerAccounts.get(owner).get(0).getLocation().getWorld();

			for (Account account : playerAccounts.get(owner)) {

				AccountStatus status = account.getStatus();
				if (!status.allowNextPayout(account.isTrustedPlayerOnline())) {
					plugin.getDatabase().addAccount(account, null);
					continue;
				}

				BigDecimal baseInterest = account.getBalance().multiply(BigDecimal.valueOf(Config.baseInterestRate))
						.setScale(2, RoundingMode.HALF_EVEN);
				BigDecimal interest = baseInterest;

				int multiplier = status.getRealMultiplier();
				if (Config.enableInterestMultipliers) {
					interest = interest.multiply(BigDecimal.valueOf(multiplier));
				}

				status.incrementMultiplier(account.isTrustedPlayerOnline());
				account.updatePrevBalance();

				final int payoutSplit = account.getTrustedPlayersCopy().size();
				for (OfflinePlayer recipient : account.getTrustedPlayersCopy()) {
					totalPayouts.put(recipient, totalPayouts.getOrDefault(recipient, BigDecimal.ZERO)
							.add(interest.divide(BigDecimal.valueOf(payoutSplit))));
					counter.put(recipient, counter.getOrDefault(recipient, 0).intValue() + 1);
				}

				plugin.getDatabase().addAccount(account, null);

				if (Config.enableInterestLog) {
					plugin.getDatabase().logInterest(account, baseInterest, multiplier, interest, null);
				}
			}

			for (OfflinePlayer recipient : totalPayouts.keySet()) {
				
				boolean online = recipient.isOnline();
				EconomyResponse r = plugin.getEconomy().depositPlayer(owner, world.getName(), totalPayouts.get(recipient).doubleValue());
				if (!r.transactionSuccess()) {
					plugin.debug("Economy transaction failed: " + r.errorMessage);
					if (online)
						recipient.getPlayer().sendMessage(Messages.ERROR_OCCURRED);
				} else
				if (online)
					recipient.getPlayer()
							.sendMessage(String.format(Messages.INTEREST_EARNED,
									Utils.formatNumber(totalPayouts.get(recipient)), counter.get(recipient),
									counter.get(recipient) == 1 ? "" : "s"));

				if (online)
					plugin.getDatabase().logLogout(owner.getPlayer(), null);

			}
		}
	}
}
