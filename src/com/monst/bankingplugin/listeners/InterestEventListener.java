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

		Map<OfflinePlayer, List<Account>> playerAccounts = accountUtils.getAccountsCopy().stream()
				.collect(Collectors.groupingBy(Account::getOwner));

		if (playerAccounts.isEmpty())
			return;

		Map<OfflinePlayer, BigDecimal> totalInterest = new HashMap<>(); // The amount of interest each account owner earns
		Map<OfflinePlayer, BigDecimal> totalFees = new HashMap<>(); // The amount of fees each account owner must pay
		Map<OfflinePlayer, Integer> interestCounter = new HashMap<>(); // The number of accounts each account owner is earning interest on
		Map<OfflinePlayer, Integer> feeCounter = new HashMap<>(); // The number of accounts each account owner must pay fees for

		Map<OfflinePlayer, BigDecimal> totalInterestBank = new HashMap<>(); // The amount of interest each bank owner must pay
		Map<OfflinePlayer, BigDecimal> totalFeesBank = new HashMap<>(); // The amount of fees each bank owner receives as income
		Map<OfflinePlayer, Integer> interestCounterBank = new HashMap<>(); // The number of accounts each bank owner must pay interest on
		Map<OfflinePlayer, Integer> feeCounterBank = new HashMap<>(); // The number of fees each bank owner receives as income

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
					feeCounter.put(owner, feeCounter.getOrDefault(owner, 0).intValue() + 0);
					if (!account.getBank().isAdminBank()) {
						totalFeesBank.put(account.getBank().getOwner(),
							totalFeesBank.getOrDefault(account.getBank().getOwner(), BigDecimal.ZERO)
									.add(BigDecimal.valueOf(config.getLowBalanceFee())));
					feeCounterBank.put(account.getBank().getOwner(),
							feeCounterBank.getOrDefault(account.getBank().getOwner(), 0).intValue() + 1);
					}
					if (Config.enableInterestLog) {
						plugin.getDatabase().logInterest(account, BigDecimal.ZERO, 0,
								BigDecimal.valueOf(config.getLowBalanceFee() * -1), null);
					}
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
				if (!account.getBank().isAdminBank()) {
					totalInterestBank.put(account.getBank().getOwner(),
							totalInterestBank.getOrDefault(account.getBank().getOwner(), BigDecimal.ZERO).add(interest));
					interestCounterBank.put(account.getBank().getOwner(),
							interestCounterBank.getOrDefault(account.getBank().getOwner(), 0).intValue() + 1);
				}

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
		
		for (OfflinePlayer bankOwner : totalInterestBank.keySet()) {
			
			if (totalInterestBank.get(bankOwner).signum() == 0)
				continue;

			boolean online = bankOwner.isOnline();
			EconomyResponse r = plugin.getEconomy().withdrawPlayer(bankOwner, online ? bankOwner.getPlayer().getWorld().getName() : world.getName(),
					totalInterestBank.get(bankOwner).doubleValue());
			if (!r.transactionSuccess()) {
				plugin.debug("Economy transaction failed: " + r.errorMessage);
				if (online)
					bankOwner.getPlayer().sendMessage(Messages.ERROR_OCCURRED);
			} else if (online)
				bankOwner.getPlayer()
						.sendMessage(String.format(Messages.INTEREST_PAID,
								Utils.formatNumber(totalInterestBank.get(bankOwner)), interestCounterBank.get(bankOwner),
								interestCounterBank.get(bankOwner) == 1 ? "" : "s"));

		}
		
		for (OfflinePlayer bankOwner : totalFeesBank.keySet()) {
			if (totalInterestBank.get(bankOwner).signum() == 0)
				continue;

			boolean online = bankOwner.isOnline();
			EconomyResponse r = plugin.getEconomy().depositPlayer(bankOwner, online ? bankOwner.getPlayer().getWorld().getName() : world.getName(),
					totalFeesBank.get(bankOwner).doubleValue());
			if (!r.transactionSuccess()) {
				plugin.debug("Economy transaction failed: " + r.errorMessage);
				if (online)
					bankOwner.getPlayer().sendMessage(Messages.ERROR_OCCURRED);
			} else if (online)
				bankOwner.getPlayer()
						.sendMessage(String.format(Messages.LOW_BALANCE_FEE_EARNED,
								Utils.formatNumber(totalFeesBank.get(bankOwner)), feeCounterBank.get(bankOwner),
								feeCounterBank.get(bankOwner) == 1 ? "" : "s"));

		}
	}
}
