package com.monst.bankingplugin.listeners;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.banking.account.Account;
import com.monst.bankingplugin.banking.account.AccountField;
import com.monst.bankingplugin.banking.bank.Bank;
import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.events.control.InterestEvent;
import com.monst.bankingplugin.lang.*;
import com.monst.bankingplugin.sql.logging.AccountInterest;
import com.monst.bankingplugin.sql.logging.BankProfit;
import com.monst.bankingplugin.utils.*;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.mariuszgromada.math.mxparser.Argument;
import org.mariuszgromada.math.mxparser.Expression;

import java.math.BigDecimal;
import java.util.Collection;
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
				Utils.map(e.getBanks(), b -> "#" + b.getID()).toString());

		Map<Bank, Set<Account>> banksAndAccounts = e.getBanks().stream()
				.map(Bank::getAccounts)
				.flatMap(Collection::stream)
				.collect(Collectors.groupingBy(Account::getBank, Collectors.toSet()));

		if (banksAndAccounts.isEmpty())
			return;

		PaymentCounter<Bank> interestPayable = new PaymentCounter<>(); // Total account interest by bank
		PaymentCounter<Bank> feesReceivable = new PaymentCounter<>(); // Total low balance fees by bank
		HashMap<Account, BigDecimal> interestReceivable = new HashMap<>(); // Interest by account
		HashMap<Account, BigDecimal> feesPayable = new HashMap<>(); // Low balance fees by account

		banksAndAccounts.forEach((bank, accounts) -> {
			for (Account account : accounts) {
				if (!account.allowNextPayout()) { // Account is frozen, pays no interest
					accountRepo.update(account, Callback.blank(),
							AccountField.DELAY_UNTIL_NEXT_PAYOUT,
							AccountField.REMAINING_OFFLINE_PAYOUTS,
							AccountField.REMAINING_OFFLINE_PAYOUTS_UNTIL_RESET
					);
					continue;
				}

				BigDecimal lowBalanceFee = BigDecimal.ZERO;
				BigDecimal interest = BigDecimal.ZERO;

				// See if account balance is below the bank minimum
				if (account.getBalance().doubleValue() < bank.getMinimumBalance().get()) {

					lowBalanceFee = BigDecimal.valueOf(bank.getLowBalanceFee().get());
					if (lowBalanceFee.signum() != 0) {
						feesPayable.put(account, lowBalanceFee); // Account must pay fee
						feesReceivable.add(account.getBank(), lowBalanceFee); // Bank receives fee
					}

					if (!bank.getPayOnLowBalance().get()) { // Bank will not pay interest since balance low
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

				BigDecimal baseInterest = QuickMath.scale(QuickMath.multiply(account.getBalance(), bank.getInterestRate().get()));
				interest = QuickMath.multiply(baseInterest, account.getRealMultiplier());

				if (interest.signum() != 0) {
					interestReceivable.put(account, interest); // Account receives interest
					interestPayable.add(account.getBank(), interest); // Bank must pay interest
				}

				account.incrementMultiplier();
				account.updatePrevBalance();

				plugin.getDatabase().logAccountInterest(new AccountInterest(
						account.getID(),
						bank.getID(),
						interest,
						lowBalanceFee,
						System.currentTimeMillis()
				));

				accountRepo.update(account, Callback.blank(),
						AccountField.MULTIPLIER_STAGE,
						AccountField.DELAY_UNTIL_NEXT_PAYOUT,
						AccountField.REMAINING_OFFLINE_PAYOUTS,
						AccountField.REMAINING_OFFLINE_PAYOUTS_UNTIL_RESET,
						AccountField.PREVIOUS_BALANCE
				);
			}
		});

		Map<Bank, BigDecimal> revenueTracker = new HashMap<>(); // Revenues by bank

		banksAndAccounts.forEach((bank, accounts) -> {

			Argument[] args = new Argument[]{
					new Argument("x", bank.getTotalValue().doubleValue()),
					new Argument("n", bank.getAccountsByOwner().size()),
					new Argument("a", bank.getAccounts().size()),
					new Argument("g", bank.getGiniCoefficient())
			};

			Expression revenueExpression = new Expression(Config.bankRevenueFunction, args);
			BigDecimal revenue = QuickMath.scale(BigDecimal.valueOf(revenueExpression.calculate()));

			if (revenue.signum() != 0)
				revenueTracker.put(bank, revenue); // Bank will receive revenue

			BigDecimal interest = interestPayable.get(bank).getTotalMoney();
			BigDecimal lowBalanceFees = feesReceivable.get(bank).getTotalMoney();

			plugin.getDatabase().logBankProfit(new BankProfit(
					bank.getID(),
					revenue,
					interest,
					lowBalanceFees,
					System.currentTimeMillis()
			));

		});

		// Sum of all profits/losses, grouped by player
		PaymentMap<OfflinePlayer> finalPayments = new PaymentMap<>();

		// Total account interest + number of accounts, grouped by player
		PaymentCounter<OfflinePlayer> totalInterestReceivableByPlayer = new PaymentCounter<>();
		// TODO: Change payment recipient to also include account co-owners
		interestReceivable.forEach((account, interest) -> {
			finalPayments.add(account.getOwner(), interest);
			totalInterestReceivableByPlayer.add(account.getOwner(), interest);
		});

		// Total account fees + number of accounts, grouped by player
		PaymentCounter<OfflinePlayer> totalFeesPayableByPlayer = new PaymentCounter<>();
		feesPayable.forEach((account, fee) -> {
			finalPayments.subtract(account.getOwner(), fee); // Subtract because payable
			totalFeesPayableByPlayer.add(account.getOwner(), fee);
		});

		// Total bank revenue + number of banks, grouped by player
		PaymentCounter<OfflinePlayer> totalRevenueReceivableByPlayer = new PaymentCounter<>();
		revenueTracker.forEach((bank, revenue) -> {
			finalPayments.add(bank.getOwner(), revenue);
			totalRevenueReceivableByPlayer.add(bank.getOwner(), revenue);
		}); // Admin banks are ignored

		// Total account interest to pay + number of accounts, grouped by player
		PaymentCounter<OfflinePlayer> totalInterestPayableByPlayer = new PaymentCounter<>();
		interestPayable.forEach((bank, interest) -> {
			finalPayments.subtract(bank.getOwner(), interest.getTotalMoney()); // Subtract because payable
			totalInterestPayableByPlayer.add(bank.getOwner(), interest);
		}); // Admin banks are ignored

		// Total account fees to receive + number of accounts, grouped by player
		PaymentCounter<OfflinePlayer> totalFeesReceivableByPlayer = new PaymentCounter<>();
		feesReceivable.forEach((bank, fees) -> {
			finalPayments.add(bank.getOwner(), fees.getTotalMoney());
			totalFeesReceivableByPlayer.add(bank.getOwner(), fees);
		}); // Admin banks are ignored

		notifyAll(totalInterestReceivableByPlayer, Message.INTEREST_PAID);
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
				PayrollOffice.submit(player, payment);
			// TODO: if payment cannot be made, ...
		});
	}

	private void notifyAll(PaymentCounter<OfflinePlayer> map, Message message) {
		map.forEach((player, counter) ->
				Mailman.notify(player, LangUtils.getMessage(message,
						new Replacement(Placeholder.AMOUNT, counter.getTotalMoney()),
						new Replacement(Placeholder.NUMBER_OF_ACCOUNTS, counter.getNumberOfPayments()),
						new Replacement(Placeholder.NUMBER_OF_BANKS, counter.getNumberOfPayments())
				))
		);
	}

	private static class PaymentMap<T> extends HashMap<T, BigDecimal> {
		private void add(T key, BigDecimal amount) {
			if (key == null)
				return;
			merge(key, amount, BigDecimal::add);
		}
		private void subtract(T key, BigDecimal amount) {
			if (key == null)
				return;
			merge(key, amount, BigDecimal::subtract);
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
