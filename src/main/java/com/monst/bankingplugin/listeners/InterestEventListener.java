package com.monst.bankingplugin.listeners;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.banking.account.Account;
import com.monst.bankingplugin.banking.bank.Bank;
import com.monst.bankingplugin.banking.bank.BankField;
import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.events.control.InterestEvent;
import com.monst.bankingplugin.lang.LangUtils;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import com.monst.bankingplugin.lang.Replacement;
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

		playerAccountMap.forEach((accountOwner, accounts) -> {
			for (Account account : accounts) {
				if (!account.getStatus().allowNextPayout(account.isTrustedPlayerOnline())) {
					accountUtils.addAccount(account, true);
					continue;
				}

				Set<OfflinePlayer> trustedPlayers = account.getTrustedPlayers();
				if (account.getBank().isPlayerBank())
					trustedPlayers.remove(account.getBank().getOwner());

				Bank bank = account.getBank();

				if (!trustedPlayers.isEmpty() && (double) bank.get(BankField.MINIMUM_BALANCE) > 0
						&& account.getBalance().compareTo(BigDecimal.valueOf((double) bank.get(BankField.MINIMUM_BALANCE))) < 0) {
					feesPayable.putIfAbsent(accountOwner, new Counter());
					feesPayable.get(accountOwner).add(BigDecimal.valueOf((double) bank.get(BankField.LOW_BALANCE_FEE)));
					if (account.getBank().isPlayerBank()) {
						feesReceivable.putIfAbsent(account.getBank().getOwner(), new Counter());
						feesReceivable.get(account.getBank().getOwner()).add(BigDecimal.valueOf((double) bank.get(BankField.LOW_BALANCE_FEE)));
					}
					if (Config.enableInterestLog)
						plugin.getDatabase().logAccountInterest(account, BigDecimal.ZERO, 0,
								BigDecimal.valueOf((double) bank.get(BankField.LOW_BALANCE_FEE) * -1), null);

					if (!(boolean) bank.get(BankField.PAY_ON_LOW_BALANCE))
						continue;
				}

				BigDecimal baseInterest = account.getBalance()
						.multiply(BigDecimal.valueOf((double) bank.get(BankField.INTEREST_RATE)))
						.setScale(2, RoundingMode.HALF_EVEN);
				BigDecimal interest = baseInterest;

				int multiplier = account.getStatus().getRealMultiplier();
				interest = interest.multiply(BigDecimal.valueOf(multiplier));

				account.getStatus().incrementMultiplier(account.isTrustedPlayerOnline());
				account.updatePrevBalance();

				if (trustedPlayers.isEmpty()) {
					accountUtils.addAccount(account, true);
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
				if (Config.enableInterestLog)
					plugin.getDatabase().logAccountInterest(account, baseInterest, multiplier, interest, null);
			}
			if (accountOwner.isOnline())
				plugin.getDatabase().logLogout(accountOwner.getPlayer(), null);
		});

		// Bank owners earn revenue on their banks
		playerBankMap.forEach((bankOwner, banks) -> {
			for (Bank bank : banks) {
				if (bank.getTotalValue().signum() == 0)
					continue;

				BigDecimal totalValue = bank.getTotalValue();
				double multiplier = Config.bankRevenueMultiplier;
				double gini = bank.getGiniCoefficient();
				int numberOfAccounts = bank.getAccountsByOwner().size();

				BigDecimal revenue = totalValue.multiply(BigDecimal.valueOf(multiplier))
						.multiply(BigDecimal.valueOf(1.0 - gini))
						.multiply(BigDecimal.valueOf(Math.log(numberOfAccounts + 1)))
						.setScale(2, RoundingMode.HALF_EVEN);

				boolean online = bankOwner.isOnline();
				Utils.depositPlayer(bankOwner, revenue.doubleValue(), Callback.of(plugin,
						result -> Utils.message(bankOwner, LangUtils.getMessage(Message.BANK_REVENUE_EARNED,
								new Replacement(Placeholder.AMOUNT, revenue),
								new Replacement(Placeholder.BANK_NAME, bank::getColorizedName)
						)),
						error -> Utils.message(bankOwner, LangUtils.getMessage(Message.ERROR_OCCURRED,
								new Replacement(Placeholder.ERROR, error::getLocalizedMessage)
						))
				));

				if (Config.enableProfitLog)
					plugin.getDatabase().logBankCashFlow(bank, revenue, null);
			}
		});

		transactAll(feesReceivable, Utils::depositPlayer, Message.LOW_BALANCE_FEE_RECEIVED); // Bank owners receive low balance fees

		transactAll(interestPayable, Utils::withdrawPlayer, Message.INTEREST_PAID); // Bank owners pay interest

		transactAll(interestReceivable, Utils::depositPlayer, Message.INTEREST_EARNED); // Account owners receive interest payments

		transactAll(feesPayable, Utils::withdrawPlayer, Message.LOW_BALANCE_FEE_PAID); // Customers pay low balance fees
	}

	private void transactAll(Map<OfflinePlayer, Counter> map, Transactor transactor, Message message) {
		for (Map.Entry<OfflinePlayer, Counter> entry : map.entrySet()) {
			OfflinePlayer customer = entry.getKey();
			Counter counter = entry.getValue();
			if (counter.getSum().signum() == 0)
				continue;

			transactor.transact(customer, counter.getSum().doubleValue(), Callback.of(plugin,
					result -> Utils.message(customer, LangUtils.getMessage(message,
							new Replacement(Placeholder.AMOUNT, counter::getSum),
							new Replacement(Placeholder.NUMBER_OF_ACCOUNTS, counter::getCount)
					)),
					error -> Utils.message(customer, LangUtils.getMessage(Message.ERROR_OCCURRED, new Replacement(Placeholder.ERROR, error::getLocalizedMessage)))
			));
		}
	}

	@FunctionalInterface
	private interface Transactor {
		void transact(OfflinePlayer player, double amount, Callback<Void> callback);
	}

	private static class Counter extends Pair<BigDecimal, Integer> {
		private Counter() {
			super(BigDecimal.ZERO, 0);
		}
		private void add(BigDecimal value) {
			super.setFirst(getSum().add(value));
			super.setSecond(getCount() + 1);
		}
		private BigDecimal getSum() { return super.getFirst(); }
		private int getCount() { return super.getSecond(); }
	}
}
