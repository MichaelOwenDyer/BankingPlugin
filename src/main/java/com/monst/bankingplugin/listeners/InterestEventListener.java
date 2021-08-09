package com.monst.bankingplugin.listeners;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.banking.Account;
import com.monst.bankingplugin.banking.AccountField;
import com.monst.bankingplugin.banking.Bank;
import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.events.control.InterestEvent;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import com.monst.bankingplugin.sql.logging.AccountInterest;
import com.monst.bankingplugin.sql.logging.BankIncome;
import com.monst.bankingplugin.utils.Pair;
import com.monst.bankingplugin.utils.PayrollOffice;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Listens for {@link InterestEvent}s and calculates the incomes and expenses for each
 * {@link OfflinePlayer} on the server.
 */
@SuppressWarnings("unused")
public class InterestEventListener extends BankingPluginListener {

	public InterestEventListener(BankingPlugin plugin) {
		super(plugin);
	}

	@EventHandler(ignoreCancelled = true)
	public void onInterestEvent(InterestEvent e) {

		if (e.getBanks().isEmpty())
			return;

		plugin.debugf("Interest payout event occurring now at bank(s) %s.",
				Utils.map(e.getBanks(), bank -> "#" + bank.getID()));

		Map<Bank, Set<Account>> banksAndAccounts = e.getBanks().stream()
				.collect(Collectors.toMap(bank -> bank, Bank::getAccounts));

		HashMap<Account, BigDecimal> accountInterest = new HashMap<>(); // Interest per account
		HashMap<Account, BigDecimal> accountFees = new HashMap<>(); // Low balance fees per account

		banksAndAccounts.forEach((bank, accounts) -> {
			for (Account account : accounts) {
				if (!account.allowNextPayout()) { // Account is frozen, pays no interest
					accountRepo.update(account,
							AccountField.DELAY_UNTIL_NEXT_PAYOUT,
							AccountField.REMAINING_OFFLINE_PAYOUTS
					);
					continue;
				}

				BigDecimal interest = BigDecimal.ZERO;
				BigDecimal lowBalanceFee = BigDecimal.ZERO;

				// See if account balance is below the bank minimum
				if (account.getBalance().compareTo(bank.minimumBalance().get()) < 0) {

					lowBalanceFee = bank.lowBalanceFee().get();
					if (lowBalanceFee.signum() != 0)
						accountFees.put(account, lowBalanceFee); // Account must pay fee

					if (!bank.payOnLowBalance().get()) { // Bank will not pay interest since balance low
						plugin.getDatabase().logAccountInterest(new AccountInterest(
								account.getID(),
								bank.getID(),
								interest,
								lowBalanceFee,
								System.currentTimeMillis()
						));
						continue;
					}
				}

				BigDecimal multipliedInterestRate = bank.interestRate().get().multiply(BigDecimal.valueOf(account.getRealMultiplier()));
				interest = account.getBalance().multiply(multipliedInterestRate).setScale(2, RoundingMode.HALF_EVEN);

				if (interest.signum() != 0)
					accountInterest.put(account, interest); // Account receives interest

				account.incrementMultiplier();
				account.updatePrevBalance();
				account.notifyObservers();

				plugin.getDatabase().logAccountInterest(new AccountInterest(
						account.getID(),
						bank.getID(),
						interest,
						lowBalanceFee,
						System.currentTimeMillis()
				));

				accountRepo.update(account,
						AccountField.MULTIPLIER_STAGE,
						AccountField.DELAY_UNTIL_NEXT_PAYOUT,
						AccountField.REMAINING_OFFLINE_PAYOUTS,
						AccountField.PREVIOUS_BALANCE
				);
			}
		});

		Map<Bank, BigDecimal> revenueTracker = new HashMap<>(); // Revenues by bank

		banksAndAccounts.forEach((bank, accounts) -> {

			BigDecimal revenue = Config.bankRevenueFunction.evaluate(
					bank.getTotalValue().doubleValue(),
					bank.getAverageValue().doubleValue(),
					bank.getAccounts().size(),
					bank.getAccountHolders().size(),
					bank.getGiniCoefficient()
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

			plugin.getDatabase().logBankIncome(new BankIncome(
					bank.getID(),
					revenue,
					interest,
					lowBalanceFees,
					System.currentTimeMillis()
			));

		});

		// Sum of all profits/losses, grouped by player
		Payroll finalPayments = new Payroll();

		// Total account interest + number of accounts, grouped by player
		PaymentCounter<OfflinePlayer> totalInterestReceivableByPlayer = new PaymentCounter<>();
		// Total account interest to pay + number of accounts, grouped by player
		PaymentCounter<OfflinePlayer> totalInterestPayableByPlayer = new PaymentCounter<>();
		// Total account fees + number of accounts, grouped by player
		PaymentCounter<OfflinePlayer> totalFeesPayableByPlayer = new PaymentCounter<>();
		// Total account fees to receive + number of accounts, grouped by player
		PaymentCounter<OfflinePlayer> totalFeesReceivableByPlayer = new PaymentCounter<>();
		// Total bank revenue + number of banks, grouped by player
		PaymentCounter<OfflinePlayer> totalRevenueReceivableByPlayer = new PaymentCounter<>();

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

		notifyAll(totalInterestReceivableByPlayer, Message.INTEREST_EARNED);
		notifyAll(totalFeesPayableByPlayer, Message.LOW_BALANCE_FEE_PAID);
		notifyAll(totalRevenueReceivableByPlayer, Message.BANK_REVENUE);
		notifyAll(totalInterestPayableByPlayer, Message.INTEREST_PAID);
		notifyAll(totalFeesReceivableByPlayer, Message.LOW_BALANCE_FEE_RECEIVED);

		// Log last seen time for all players receiving a message
		plugin.getDatabase().logLastSeen(
				finalPayments.keySet().stream()
						.filter(OfflinePlayer::isOnline)
						.map(OfflinePlayer::getPlayer)
						.collect(Collectors.toSet())
		);

		finalPayments.forEach((player, payment) -> {
			if (PayrollOffice.allowPayment(player, payment))
				PayrollOffice.deposit(player, payment);
			// TODO: if payment cannot be made, ...
		});
	}

	private void notifyAll(PaymentCounter<OfflinePlayer> map, Message message) {
		map.forEach((player, counter) -> Utils.notify(player, message
				.with(Placeholder.AMOUNT).as(counter.getTotalMoney())
				.and(Placeholder.NUMBER_OF_ACCOUNTS).as(counter.getNumberOfPayments())
				.and(Placeholder.NUMBER_OF_BANKS).as(counter.getNumberOfPayments())
				.translate())
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

	private static class PaymentCounter<T> extends HashMap<T, Counter> {
		private void add(T key, BigDecimal amount) {
			if (key == null)
				return;
			merge(key, new Counter(amount), Counter::add);
		}
		private void add(T key, Counter counter) {
			if (key == null)
				return;
			merge(key, counter, Counter::add);
		}
	}

	private static class Counter extends Pair<BigDecimal, Integer> {
		private static final Counter EMPTY = new Counter();
		private Counter() {
			super(BigDecimal.ZERO, 0);
		}
		private Counter(BigDecimal value) {
			super(value, 1);
		}
		private Counter add(double value) {
			return add(BigDecimal.valueOf(value));
		}
		private Counter add(BigDecimal value) {
			super.setFirst(getTotalMoney().add(value));
			super.setSecond(getNumberOfPayments() + 1);
			return this;
		}
		private Counter add(Counter counter) {
			super.setFirst(getTotalMoney().add(counter.getTotalMoney()));
			super.setSecond(getNumberOfPayments() + counter.getNumberOfPayments());
			return this;
		}
		private BigDecimal getTotalMoney() { return super.getFirst(); }
		private int getNumberOfPayments() { return super.getSecond(); }
	}
}
