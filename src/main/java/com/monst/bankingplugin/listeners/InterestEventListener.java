package com.monst.bankingplugin.listeners;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.banking.account.Account;
import com.monst.bankingplugin.banking.bank.Bank;
import com.monst.bankingplugin.banking.bank.BankConfig;
import com.monst.bankingplugin.banking.bank.BankField;
import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.events.control.InterestEvent;
import com.monst.bankingplugin.utils.*;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Listens for {@link InterestEvent}s and calculates the incomes and expenses for each
 * {@link OfflinePlayer} on the server.
 */
@SuppressWarnings("unused")
public class InterestEventListener implements Listener {
	
	private final BankingPlugin plugin;
	private final AccountUtils accountUtils;

	public InterestEventListener(BankingPlugin plugin) {
		this.plugin = plugin;
		this.accountUtils = plugin.getAccountUtils();
	}

	@EventHandler(ignoreCancelled = true)
	public void onInterestEvent(InterestEvent e) {

		if (e.getBanks().isEmpty())
			return;

		plugin.debug("Interest payout event occurring now at bank(s) " +
				Utils.map(e.getBanks(), b -> "#" + b.getID()).toString());

		Map<OfflinePlayer, List<Bank>> playerBankMap = e.getBanks().stream()
				.filter(Bank::isPlayerBank)
				.collect(Collectors.groupingBy(Bank::getOwner));
		Map<OfflinePlayer, List<Account>> playerAccountMap = e.getBanks().stream()
				.map(Bank::getAccounts)
				.flatMap(Collection::stream)
				.collect(Collectors.groupingBy(Account::getOwner));

		if (playerAccountMap.isEmpty())
			return;

		Map<OfflinePlayer, Counter> interestReceivable = new HashMap<>(); // The amount of interest each account owner earns on how many accounts
		Map<OfflinePlayer, Counter> feesPayable = new HashMap<>(); // The amount of fees each account owner must pay for how many accounts

		Map<OfflinePlayer, Counter> interestPayable = new HashMap<>(); // The amount of interest each bank owner must pay for how many banks
		Map<OfflinePlayer, Counter> feesReceivable = new HashMap<>(); // The amount of fees each bank owner receives as income on how many banks

		for (OfflinePlayer accountOwner : playerAccountMap.keySet()) {
			for (Account account : playerAccountMap.get(accountOwner)) {
				if (!account.getStatus().allowNextPayout(account.isTrustedPlayerOnline())) {
					accountUtils.addAccount(account, true);
					continue;
				}

				Set<OfflinePlayer> trustedPlayers = account.getTrustedPlayers();
				if (account.getBank().isPlayerBank())
					trustedPlayers.remove(account.getBank().getOwner());

				BankConfig config = account.getBank().getConfig();

				if (!trustedPlayers.isEmpty() && (double) config.get(BankField.MINIMUM_BALANCE) > 0
						&& account.getBalance().compareTo(BigDecimal.valueOf((double) config.get(BankField.MINIMUM_BALANCE))) < 0) {
					feesPayable.putIfAbsent(accountOwner, new Counter());
					feesPayable.get(accountOwner).add(BigDecimal.valueOf((double) config.get(BankField.LOW_BALANCE_FEE)));
					if (account.getBank().isPlayerBank()) {
						feesReceivable.putIfAbsent(account.getBank().getOwner(), new Counter());
						feesReceivable.get(account.getBank().getOwner()).add(BigDecimal.valueOf((double) config.get(BankField.LOW_BALANCE_FEE)));
					}
					if (Config.enableInterestLog)
						plugin.getDatabase().logInterest(account, BigDecimal.ZERO, 0,
								BigDecimal.valueOf((double) config.get(BankField.LOW_BALANCE_FEE) * -1), null);

					if (!(boolean) config.get(BankField.PAY_ON_LOW_BALANCE))
						continue;
				}

				BigDecimal baseInterest = account.getBalance()
						.multiply(BigDecimal.valueOf((double) config.get(BankField.INTEREST_RATE)))
						.setScale(2, RoundingMode.HALF_EVEN);
				BigDecimal interest = baseInterest;

				int multiplier = account.getStatus().getRealMultiplier();
				interest = interest.multiply(BigDecimal.valueOf(multiplier));

				account.getStatus().incrementMultiplier(account.isTrustedPlayerOnline());
				account.updatePrevBalance();

					accountUtils.addAccount(account, true);
				if (trustedPlayers.isEmpty()) {
					continue;
				}

				BigDecimal cut = interest.divide(BigDecimal.valueOf(trustedPlayers.size()), RoundingMode.HALF_EVEN);
				for (OfflinePlayer recipient : trustedPlayers) {
					interestReceivable.putIfAbsent(recipient, new Counter());
					interestReceivable.get(recipient).add(cut);
				}

				if (account.getBank().isPlayerBank()) {
					interestPayable.putIfAbsent(account.getBank().getOwner(), new Counter());
					interestPayable.get(account.getBank().getOwner()).add(interest);
				}

				accountUtils.addAccount(account, true);

				if (Config.enableInterestLog) {
					plugin.getDatabase().logInterest(account, baseInterest, multiplier, interest, null);
				}
			}
			if (accountOwner.isOnline())
				plugin.getDatabase().logLogout(accountOwner.getPlayer(), null);
		}

		// Bank owners earn revenue on their banks
		for (OfflinePlayer bankOwner : playerBankMap.keySet()) {
			for (Bank bank : playerBankMap.get(bankOwner)) {
				if (bank.getTotalValue().signum() == 0)
					continue;

				BigDecimal totalValue = bank.getTotalValue();
				double multiplier = Config.bankRevenueMultiplier;
				double gini = BankUtils.getGiniCoefficient(bank);
				int numberOfAccounts = bank.getAccountsByOwner().size();

				BigDecimal revenue = totalValue.multiply(BigDecimal.valueOf(multiplier))
						.multiply(BigDecimal.valueOf(1.0d - gini))
						.multiply(BigDecimal.valueOf(Math.log(numberOfAccounts + 1)))
						.setScale(2, RoundingMode.HALF_EVEN);

				boolean online = bankOwner.isOnline();
				Utils.depositPlayer(bankOwner, bank.getSelection().getWorld().getName(), revenue.doubleValue(), new Callback<Void>(plugin) {
					@Override
					public void onResult(Void result) {
						if (online)
							bankOwner.getPlayer().sendMessage(String.format(Messages.REVENUE_EARNED,
									Utils.format(revenue), bank.getName()));
					}
					@Override
					public void onError(Throwable throwable) {
						super.onError(throwable);
						if (online)
							bankOwner.getPlayer().sendMessage(Messages.ERROR_OCCURRED);
					}
				});
			}
		}

		String fallbackWorldName = playerAccountMap.values().stream().flatMap(Collection::stream)
				.collect(Collectors.toList()).get(0).getLocation().getWorld().getName();

		// Bank owners receive low balance fees
		for (OfflinePlayer bankOwner : feesReceivable.keySet()) {
			if (feesReceivable.get(bankOwner).getSum().signum() == 0)
				continue;
			if (!bankOwner.hasPlayedBefore())
				continue;

			boolean isOnline = bankOwner.isOnline();
			String worldName = isOnline ? bankOwner.getPlayer().getWorld().getName() : fallbackWorldName;
			Utils.depositPlayer(bankOwner, worldName, feesReceivable.get(bankOwner).getSum().doubleValue(), new Callback<Void>(plugin) {
				@Override
				public void onResult(Void result) {
					if (isOnline) {
						int count = feesReceivable.get(bankOwner).getCount();
						bankOwner.getPlayer().sendMessage(String.format(Messages.LOW_BALANCE_FEE_EARNED,
								Utils.format(feesReceivable.get(bankOwner).getSum()),
								count, count == 1 ? "" : "s"));
					}
				}
				@Override
				public void onError(Throwable throwable) {
					super.onError(throwable);
					if (isOnline)
						bankOwner.getPlayer().sendMessage(Messages.ERROR_OCCURRED);
				}
			});
		}

		// Bank owners pay interest
		for (OfflinePlayer bankOwner : interestPayable.keySet()) {
			if (interestPayable.get(bankOwner).getSum().signum() == 0)
				continue;
			if (!bankOwner.hasPlayedBefore())
				continue;

			boolean isOnline = bankOwner.isOnline();
			String worldName = isOnline ? bankOwner.getPlayer().getWorld().getName() : fallbackWorldName;

			Utils.withdrawPlayer(bankOwner, worldName, interestPayable.get(bankOwner).getSum().doubleValue(), new Callback<Void>(plugin) {
				@Override
				public void onResult(Void result) {
					if (isOnline) {
						int count = interestPayable.get(bankOwner).getCount();
						bankOwner.getPlayer().sendMessage(String.format(Messages.INTEREST_PAID,
								Utils.format(interestPayable.get(bankOwner).getSum()),
								count, count == 1 ? "" : "s"));
					}
				}
				@Override
				public void onError(Throwable throwable) {
					super.onError(throwable);
					if (isOnline)
						bankOwner.getPlayer().sendMessage(Messages.ERROR_OCCURRED);
				}
			});
		}

		// Account owners receive interest payments
		for (OfflinePlayer customer : interestReceivable.keySet()) {
			if (interestReceivable.get(customer).getSum().signum() == 0)
				continue;

			boolean online = customer.isOnline();
			String worldName = playerAccountMap.get(customer) != null
					? playerAccountMap.get(customer).get(0).getLocation().getWorld().getName()
					: (online ? customer.getPlayer().getWorld().getName() : fallbackWorldName);

			Utils.depositPlayer(customer, worldName, interestReceivable.get(customer).getSum().doubleValue(), new Callback<Void>(plugin) {
				@Override
				public void onResult(Void result) {
					if (online) {
						int count = interestReceivable.get(customer).getCount();
						customer.getPlayer().sendMessage(String.format(Messages.INTEREST_EARNED,
								Utils.format(interestReceivable.get(customer).getSum()),
								count, count == 1 ? "" : "s"));
					}
				}
				@Override
				public void onError(Throwable throwable) {
					super.onError(throwable);
					if (online)
						customer.getPlayer().sendMessage(Messages.ERROR_OCCURRED);
				}
			});
		}

		// Customers pay low balance fees
		for (OfflinePlayer customer : feesPayable.keySet()) {
			if (feesPayable.get(customer).getSum().signum() == 0)
				continue;

			boolean online = customer.isOnline();
			String worldName = playerAccountMap.get(customer) != null
					? playerAccountMap.get(customer).get(0).getLocation().getWorld().getName()
					: (online ? customer.getPlayer().getWorld().getName() : fallbackWorldName);

			Utils.withdrawPlayer(customer, worldName, feesPayable.get(customer).getSum().doubleValue(), new Callback<Void>(plugin) {
				@Override
				public void onResult(Void result) {
					if (online) {
						int count = feesPayable.get(customer).getCount();
						customer.getPlayer().sendMessage(String.format(Messages.LOW_BALANCE_FEE_PAID,
								Utils.format(feesPayable.get(customer).getSum()),
								count, count == 1 ? "" : "s"));
					}
				}
				@Override
				public void onError(Throwable throwable) {
					super.onError(throwable);
					if (online)
						customer.getPlayer().sendMessage(Messages.ERROR_OCCURRED);
				}
			});
		}
	}

	private static class Counter extends Pair<BigDecimal, Integer> {
		private Counter() {
			super(BigDecimal.ZERO, 0);
		}
		private void add(BigDecimal value) {
			super.setFirst(getSum().add(value));
			super.setSecond(getCount() + 1);
		}
		private BigDecimal getSum() {
			return super.getFirst();
		}
		private int getCount() {
			return super.getSecond();
		}
	}
}
