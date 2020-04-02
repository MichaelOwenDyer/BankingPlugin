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
import com.monst.bankingplugin.utils.AccountConfig;
import com.monst.bankingplugin.utils.AccountStatus;
import com.monst.bankingplugin.utils.AccountUtils;
import com.monst.bankingplugin.utils.BankUtils;
import com.monst.bankingplugin.utils.Messages;
import com.monst.bankingplugin.utils.Utils;

import net.milkbowl.vault.economy.EconomyResponse;

public class InterestEventListener implements Listener {
	
	private BankingPlugin plugin;
	private AccountUtils accountUtils;
	private BankUtils bankUtils;
	
	public InterestEventListener(BankingPlugin plugin) {
		this.plugin = plugin;
		this.accountUtils = plugin.getAccountUtils();
		this.bankUtils = plugin.getBankUtils();
	}

	@EventHandler
	public void onInterestEvent(InterestEvent e) {
		
		plugin.debug("Interest payout event occurring now!");

		Map<OfflinePlayer, BigDecimal> totalInterest = new HashMap<>();
		Map<OfflinePlayer, BigDecimal> totalFees = new HashMap<>();
		Map<OfflinePlayer, Integer> interestCounter = new HashMap<>();
		Map<OfflinePlayer, Integer> feeCounter = new HashMap<>();

		Map<OfflinePlayer, BigDecimal> totalInterestBank = new HashMap<>();
		Map<OfflinePlayer, BigDecimal> totalFeesBank = new HashMap<>();
		Map<OfflinePlayer, Integer> interestCounterBank = new HashMap<>();
		Map<OfflinePlayer, Integer> feeCounterBank = new HashMap<>();

		Map<OfflinePlayer, List<Account>> playerAccounts = accountUtils.getAccountsCopy().stream()
				.collect(Collectors.groupingBy(Account::getOwner));

		for (OfflinePlayer owner : playerAccounts.keySet()) {
			
			for (Account account : playerAccounts.get(owner)) {

				AccountConfig config = account.getBank().getAccountConfig();
				AccountStatus status = account.getStatus();
				if (!status.allowNextPayout(account.isTrustedPlayerOnline())) {
					plugin.getDatabase().addAccount(account, null);
					continue;
				}

				if (config.getMinBalance() > 0 && account.getBalance().compareTo(BigDecimal.valueOf(config.getMinBalance())) == -1) {
					totalFees.put(owner, totalFees.getOrDefault(owner, BigDecimal.ZERO)
							.add(BigDecimal.valueOf(config.getLowBalanceFee())));
					totalFeesBank.put(account.getBank().getOwner(),
							totalFeesBank.getOrDefault(account.getBank().getOwner(), BigDecimal.ZERO)
									.add(BigDecimal.valueOf(config.getLowBalanceFee())));
					if (Config.enableInterestLog) {
						plugin.getDatabase().logInterest(account, BigDecimal.ZERO, 0,
								BigDecimal.valueOf(config.getLowBalanceFee() * -1), null);
					}
					feeCounter.put(owner, feeCounter.getOrDefault(owner, 0).intValue() + 0);
					feeCounterBank.put(account.getBank().getOwner(),
							feeCounterBank.getOrDefault(account.getBank().getOwner(), 0).intValue() + 1);
					continue;
				}

				BigDecimal baseInterest = account.getBalance()
						.multiply(BigDecimal.valueOf(config.getInterestRate()))
						.setScale(2, RoundingMode.HALF_EVEN);
				BigDecimal interest = baseInterest;

				int multiplier = status.getRealMultiplier();
				interest = interest.multiply(BigDecimal.valueOf(multiplier));

				status.incrementMultiplier(account.isTrustedPlayerOnline());
				account.updatePrevBalance();

				final int payoutSplit = account.getTrustedPlayersCopy().size();
				for (OfflinePlayer recipient : account.getTrustedPlayersCopy()) {
					totalInterest.put(recipient, totalInterest.getOrDefault(recipient, BigDecimal.ZERO)
							.add(interest.divide(BigDecimal.valueOf(payoutSplit))));
					interestCounter.put(recipient, interestCounter.getOrDefault(recipient, 0).intValue() + 1);
				}
				totalInterestBank.put(account.getBank().getOwner(),
						totalInterestBank.getOrDefault(account.getBank().getOwner(), BigDecimal.ZERO).add(interest));
				interestCounterBank.put(account.getBank().getOwner(),
						interestCounterBank.getOrDefault(account.getBank().getOwner(), 0).intValue() + 1);

				plugin.getDatabase().addAccount(account, null);

				if (Config.enableInterestLog) {
					plugin.getDatabase().logInterest(account, baseInterest, multiplier, interest, null);
				}

			}
			if (owner.isOnline())
				plugin.getDatabase().logLogout(owner.getPlayer(), null);
		}

		World world = ((Account) playerAccounts.values().toArray()[0]).getLocation().getWorld();

		for (OfflinePlayer customer : totalInterest.keySet()) {

			if (totalInterest.get(customer).signum() == 0)
				continue;

			boolean online = customer.isOnline();
			EconomyResponse r = plugin.getEconomy().depositPlayer(customer, online ? customer.getPlayer().getWorld().getName() : world.getName(),
					totalInterest.get(customer).doubleValue());
			if (!r.transactionSuccess()) {
				plugin.debug("Economy transaction failed: " + r.errorMessage);
				if (online)
					customer.getPlayer().sendMessage(Messages.ERROR_OCCURRED);
			} else if (online)
				customer.getPlayer()
						.sendMessage(String.format(Messages.INTEREST_EARNED,
								Utils.formatNumber(totalInterest.get(customer)), interestCounter.get(customer),
								interestCounter.get(customer) == 1 ? "" : "s"));

		}

		for (OfflinePlayer customer : totalFees.keySet()) {

			if (totalInterest.get(customer).signum() == 0)
				continue;

			boolean online = customer.isOnline();
			EconomyResponse r = plugin.getEconomy().withdrawPlayer(customer, online ? customer.getPlayer().getWorld().getName() : world.getName(),
					totalFees.get(customer).doubleValue());
			if (!r.transactionSuccess()) {
				plugin.debug("Economy transaction failed: " + r.errorMessage);
				if (online)
					customer.getPlayer().sendMessage(Messages.ERROR_OCCURRED);
			} else if (online)
				customer.getPlayer()
						.sendMessage(String.format(Messages.LOW_BALANCE_FEE_PAID,
								Utils.formatNumber(totalFees.get(customer)), feeCounter.get(customer),
								feeCounter.get(customer) == 1 ? "" : "s"));
		}
		
		for (OfflinePlayer owner : totalInterestBank.keySet()) {
			
			if (totalInterestBank.get(owner).signum() == 0)
				continue;

			boolean online = owner.isOnline();
			EconomyResponse r = plugin.getEconomy().withdrawPlayer(owner, online ? owner.getPlayer().getWorld().getName() : world.getName(),
					totalInterestBank.get(owner).doubleValue());
			if (!r.transactionSuccess()) {
				plugin.debug("Economy transaction failed: " + r.errorMessage);
				if (online)
					owner.getPlayer().sendMessage(Messages.ERROR_OCCURRED);
			} else if (online)
				owner.getPlayer()
						.sendMessage(String.format(Messages.INTEREST_PAID,
								Utils.formatNumber(totalInterestBank.get(owner)), interestCounterBank.get(owner),
								interestCounterBank.get(owner) == 1 ? "" : "s"));

		}
		
		for (OfflinePlayer owner : totalFeesBank.keySet()) {

			if (totalInterestBank.get(owner).signum() == 0)
				continue;

			boolean online = owner.isOnline();
			EconomyResponse r = plugin.getEconomy().depositPlayer(owner, online ? owner.getPlayer().getWorld().getName() : world.getName(),
					totalFeesBank.get(owner).doubleValue());
			if (!r.transactionSuccess()) {
				plugin.debug("Economy transaction failed: " + r.errorMessage);
				if (online)
					owner.getPlayer().sendMessage(Messages.ERROR_OCCURRED);
			} else if (online)
				owner.getPlayer()
						.sendMessage(String.format(Messages.LOW_BALANCE_FEE_EARNED,
								Utils.formatNumber(totalFeesBank.get(owner)), feeCounterBank.get(owner),
								feeCounterBank.get(owner) == 1 ? "" : "s"));

		}
	}
}
