package com.monst.bankingplugin.listener;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.entity.Account;
import com.monst.bankingplugin.entity.Bank;
import com.monst.bankingplugin.entity.log.AccountInterest;
import com.monst.bankingplugin.entity.log.BankIncome;
import com.monst.bankingplugin.event.control.InterestEvent;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
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
public class InterestEventListener implements Listener {

	private final BankingPlugin plugin;

	public InterestEventListener(BankingPlugin plugin) {
		this.plugin = plugin;
	}

	@EventHandler(ignoreCancelled = true)
	public void onInterestEvent(InterestEvent e) {
		if (e.getBanks().isEmpty())
			return;

		plugin.debugf("Interest payout event occurring now at bank(s) %s.", e.getBanks());

		Map<Bank, Set<Account>> banksAndAccounts = e.getBanks().stream()
				.collect(Collectors.toMap(bank -> bank, Bank::getAccounts));

		Map<Account, BigDecimal> accountInterest = new HashMap<>(); // Interest per account
		Map<Account, BigDecimal> accountFees = new HashMap<>(); // Low balance fees per account

		List<AccountInterest> interests = new LinkedList<>();
		List<BankIncome> incomes = new LinkedList<>();

		banksAndAccounts.forEach((bank, accounts) -> {
			for (Account account : accounts) {

				boolean trustedPlayerOnline = account.getTrustedPlayers().stream().anyMatch(OfflinePlayer::isOnline);
				if (trustedPlayerOnline) {
					account.setRemainingOfflinePayouts(plugin.config().allowedOfflinePayouts.at(bank));
				} else {
					if (account.getRemainingOfflinePayouts() > 0)
						account.setRemainingOfflinePayouts(account.getRemainingOfflinePayouts() - 1);
					if (account.getRemainingOfflinePayouts() == 0)
						continue;
				}

				BigDecimal interest = BigDecimal.ZERO;
				BigDecimal lowBalanceFee = BigDecimal.ZERO;

				// See if account balance is below the bank minimum
				if (account.getBalance().compareTo(plugin.config().minimumBalance.at(bank)) < 0) {

					lowBalanceFee = plugin.config().lowBalanceFee.at(bank);
					if (lowBalanceFee.signum() != 0)
						accountFees.put(account, lowBalanceFee); // Account must pay fee

					if (!plugin.config().payOnLowBalance.at(bank)) { // Bank will not pay interest since balance low
						interests.add(new AccountInterest(account, bank, interest, lowBalanceFee));
						continue;
					}
				}

				{
					BigDecimal balance = account.getBalance();
					BigDecimal interestRate = plugin.config().interestRate.at(bank);
					BigDecimal multiplier = BigDecimal.valueOf(account.getInterestMultiplier(plugin.config().interestMultipliers.at(bank)));
					interest = balance.multiply(interestRate).multiply(multiplier).setScale(2, RoundingMode.HALF_EVEN);
				}

				if (interest.signum() != 0)
					accountInterest.put(account, interest); // Account receives interest

				if (trustedPlayerOnline)
					account.incrementMultiplierByOne(plugin.config().interestMultipliers.at(bank).size() - 1);
				else
					account.decrementMultiplier(plugin.config().offlineMultiplierDecrement.at(bank));
				account.updatePreviousBalance();

				interests.add(new AccountInterest(account, bank, interest, lowBalanceFee));

			}
		});

		for (Set<Account> accounts : banksAndAccounts.values())
			plugin.getAccountService().updateAll(accounts); // Update accounts by-bank TODO: altogether?

		Map<Bank, BigDecimal> revenueTracker = new HashMap<>(); // Revenues by bank

		banksAndAccounts.forEach((bank, accounts) -> {

			BigDecimal revenue = plugin.config().bankRevenueExpression.evaluate(
					bank.getTotalValue().doubleValue(),
					bank.getAverageValue().doubleValue(),
					bank.getNumberOfAccounts(),
					bank.getAccountHolders().size(),
					bank.getGiniCoefficient().doubleValue()
			);

			if (revenue.signum() != 0)
				revenueTracker.put(bank, revenue); // Bank will receive revenue

			BigDecimal interest = accounts.stream()
					.filter(accountInterest::containsKey)
					.map(accountInterest::get)
					.reduce(BigDecimal.ZERO, BigDecimal::add);
			BigDecimal lowBalanceFees = accounts.stream()
					.filter(accountFees::containsKey)
					.map(accountFees::get)
					.reduce(BigDecimal.ZERO, BigDecimal::add);

			incomes.add(new BankIncome(bank, revenue, interest, lowBalanceFees));

		});

		plugin.getAccountInterestService().saveAll(interests);
		plugin.getBankIncomeService().saveAll(incomes);

		// Total account interest + number of accounts, grouped by player
		PaymentCounter totalInterestReceivableByPlayer = new PaymentCounter();
		// Total account interest to pay + number of accounts, grouped by player
		PaymentCounter totalInterestPayableByPlayer = new PaymentCounter();
		// Total account fees + number of accounts, grouped by player
		PaymentCounter totalFeesPayableByPlayer = new PaymentCounter();
		// Total account fees to receive + number of accounts, grouped by player
		PaymentCounter totalFeesReceivableByPlayer = new PaymentCounter();
		// Total bank revenue + number of banks, grouped by player
		PaymentCounter totalRevenueReceivableByPlayer = new PaymentCounter();
		// Sum of all profits/losses, grouped by player
		Payroll finalPayments = new Payroll();

		// TODO: Change payment recipient to also include account co-owners
		accountInterest.forEach((account, interest) -> {
			if (account.getBank().isOwner(account.getOwner()))
				return;
			finalPayments.add(account.getOwner(), interest);
			finalPayments.subtract(account.getBank().getOwner(), interest);
			totalInterestReceivableByPlayer.add(account.getOwner(), interest);
			totalInterestPayableByPlayer.add(account.getBank().getOwner(), interest);
		});

		accountFees.forEach((account, fee) -> {
			if (account.getBank().isOwner(account.getOwner()))
				return;
			finalPayments.subtract(account.getOwner(), fee);
			finalPayments.add(account.getBank().getOwner(), fee);
			totalFeesPayableByPlayer.add(account.getOwner(), fee);
			totalFeesReceivableByPlayer.add(account.getBank().getOwner(), fee);
		});

		revenueTracker.forEach((bank, revenue) -> {
			finalPayments.add(bank.getOwner(), revenue);
			totalRevenueReceivableByPlayer.add(bank.getOwner(), revenue);
		}); // Admin banks are ignored

		notifyAll(totalFeesPayableByPlayer, Message.ACCOUNT_LOW_BALANCE_FEES_PAID);
		notifyAll(totalInterestReceivableByPlayer, Message.ACCOUNT_INTEREST_EARNED);
		notifyAll(totalInterestPayableByPlayer, Message.ACCOUNT_INTEREST_PAID);
		notifyAll(totalFeesReceivableByPlayer, Message.ACCOUNT_LOW_BALANCE_FEES_RECEIVED);
		notifyAll(totalRevenueReceivableByPlayer, Message.BANK_REVENUE_EARNED);

		// Log last seen time for all players receiving a message
		plugin.getLastSeenService().updateLastSeenTime(finalPayments.keySet());

		finalPayments.forEach((player, payment) -> {
			plugin.getPaymentService().transact(player, payment.doubleValue());
			// TODO: if payment cannot be made, ...
		});
	}

	private void notifyAll(PaymentCounter map, Message message) {
		map.forEach((player, counter) -> {
					if (!player.isOnline())
						return;
					player.getPlayer().sendMessage(message
							.with(Placeholder.AMOUNT).as(plugin.getEconomy().format(counter.total.doubleValue()))
							.and(Placeholder.NUMBER_OF_ACCOUNTS).as(counter.paymentCount)
							.and(Placeholder.NUMBER_OF_BANKS).as(counter.paymentCount)
							.translate(plugin));
				}
		);
	}

	private static class Payroll extends HashMap<OfflinePlayer, BigDecimal> {
		private void add(OfflinePlayer player, BigDecimal amount) {
			if (player == null)
				return;
			merge(player, amount, BigDecimal::add);
		}
		private void subtract(OfflinePlayer player, BigDecimal amount) {
			if (player == null)
				return;
			merge(player, amount, BigDecimal::subtract);
		}
	}

	private static class PaymentCounter extends HashMap<OfflinePlayer, Counter> {
		private void add(OfflinePlayer key, BigDecimal amount) {
			if (key == null)
				return;
			merge(key, new Counter(amount), Counter::add);
		}
	}

	private static class Counter {
		private BigDecimal total;
		private int paymentCount;
		private Counter(BigDecimal value) {
			this.total = value;
			this.paymentCount = 1;
		}
		private Counter add(Counter counter) {
			total = total.add(counter.total);
			paymentCount += counter.paymentCount;
			return this;
		}
	}
}
