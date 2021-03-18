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
import com.monst.bankingplugin.utils.Callback;
import com.monst.bankingplugin.utils.Pair;
import com.monst.bankingplugin.utils.QuickMath;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.mariuszgromada.math.mxparser.Argument;
import org.mariuszgromada.math.mxparser.Expression;

import java.math.BigDecimal;
import java.util.*;
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

		Map<OfflinePlayer, List<Bank>> playerBankMap = e.getBanks().stream()
				.filter(Bank::isPlayerBank)
				.collect(Collectors.groupingBy(Bank::getOwner));
		Map<OfflinePlayer, List<Account>> playerAccountMap = e.getBanks().stream()
				.map(Bank::getAccounts)
				.flatMap(Collection::stream)
				.collect(Collectors.groupingBy(Account::getOwner));

		if (playerAccountMap.isEmpty())
			return;

		MoneyTracker interestReceivable = new MoneyTracker(); // The amount of interest each account owner earns on how many accounts
		MoneyTracker feesPayable = new MoneyTracker(); // The amount of fees each account owner must pay for how many accounts

		MoneyTracker interestPayable = new MoneyTracker(); // The amount of interest each bank owner must pay for how many banks
		MoneyTracker feesReceivable = new MoneyTracker(); // The amount of fees each bank owner receives as income on how many banks

		playerAccountMap.forEach((accountOwner, accounts) -> {
			for (Account account : accounts) {
				if (!account.allowNextPayout()) {
					accountRepo.update(account, Callback.blank(),
							AccountField.DELAY_UNTIL_NEXT_PAYOUT,
							AccountField.REMAINING_OFFLINE_PAYOUTS,
							AccountField.REMAINING_OFFLINE_PAYOUTS_UNTIL_RESET
					);
					continue;
				}

				BigDecimal lowBalanceFee = BigDecimal.ZERO;
				BigDecimal interest = BigDecimal.ZERO;
				Set<OfflinePlayer> trustedPlayers = account.getTrustedPlayers();
				int numberOfTrustedPlayers = trustedPlayers.size();
				Bank bank = account.getBank();
				if (bank.isPlayerBank())
					trustedPlayers.remove(bank.getOwner()); // Bank owner should not be considered, since he would be paying himself

				// trustedPlayers would be empty if the bank owner was the only trusted player
				if (!trustedPlayers.isEmpty() && bank.getMinimumBalance().get() > 0
						&& account.getBalance().doubleValue() < bank.getMinimumBalance().get()) {

					lowBalanceFee = BigDecimal.valueOf(bank.getLowBalanceFee().get());
					feesPayable.get(accountOwner).add(lowBalanceFee);
					if (bank.isPlayerBank())
						feesReceivable.get(bank.getOwner()).add(lowBalanceFee);

					if (!bank.getPayOnLowBalance().get()) {
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

				BigDecimal baseInterest = QuickMath.scale(
						QuickMath.multiply(account.getBalance(), bank.getInterestRate().get())
				);

				int multiplier = account.getRealMultiplier();
				interest = QuickMath.multiply(baseInterest, multiplier);

				account.incrementMultiplier();
				account.updatePrevBalance();

				if (!trustedPlayers.isEmpty()) {
					BigDecimal cut = QuickMath.divide(interest, numberOfTrustedPlayers);
					for (OfflinePlayer recipient : trustedPlayers)
						interestReceivable.get(recipient).add(cut);

					if (account.getBank().isPlayerBank())
						interestPayable.get(account.getBank().getOwner()).add(interest);

					plugin.getDatabase().logAccountInterest(new AccountInterest(
							account.getID(),
							bank.getID(),
							interest,
							lowBalanceFee,
							System.currentTimeMillis()
					));
				}

				accountRepo.update(account, Callback.blank(),
						AccountField.MULTIPLIER_STAGE,
						AccountField.DELAY_UNTIL_NEXT_PAYOUT,
						AccountField.REMAINING_OFFLINE_PAYOUTS,
						AccountField.REMAINING_OFFLINE_PAYOUTS_UNTIL_RESET,
						AccountField.PREVIOUS_BALANCE
				);
			}

			if (accountOwner.isOnline())
				plugin.getDatabase().logLastSeen(accountOwner.getPlayer());
		});

		// Bank owners earn revenue on their banks
		playerBankMap.forEach((bankOwner, banks) -> {
			for (Bank bank : banks) {

				Argument[] args = new Argument[] {
						new Argument("x", bank.getTotalValue().doubleValue()),
						new Argument("n", bank.getAccountsByOwner().size()),
						new Argument("a", bank.getAccounts().size()),
						new Argument("g", bank.getGiniCoefficient())
				};

				Expression revenueExpression = new Expression(Config.bankRevenueFunction, args);
				BigDecimal revenue = QuickMath.scale(BigDecimal.valueOf(revenueExpression.calculate()));

				if (revenue.signum() == 0)
					continue;

				boolean online = bankOwner.isOnline();
				Utils.depositPlayer(bankOwner, revenue.doubleValue(), Callback.of(
                        result -> Mailman.notify(bankOwner, LangUtils.getMessage(Message.BANK_PROFIT,
								new Replacement(Placeholder.AMOUNT, revenue),
								new Replacement(Placeholder.BANK_NAME, bank::getColorizedName)
						)),
						error -> Mailman.notify(bankOwner, LangUtils.getMessage(Message.ERROR_OCCURRED,
								new Replacement(Placeholder.ERROR, error::getLocalizedMessage)
						))
				));

				plugin.getDatabase().logBankProfit(new BankProfit(
						bank.getID(),
						revenue,
						System.currentTimeMillis()
				));
			}
		});

		transactAll(feesReceivable, Utils::depositPlayer, Message.LOW_BALANCE_FEE_RECEIVED); // Bank owners receive low balance fees

		transactAll(interestPayable, Utils::withdrawPlayer, Message.INTEREST_PAID); // Bank owners pay interest

		transactAll(interestReceivable, Utils::depositPlayer, Message.INTEREST_EARNED); // Account owners receive interest payments

		transactAll(feesPayable, Utils::withdrawPlayer, Message.LOW_BALANCE_FEE_PAID); // Account owners pay low balance fees
	}

	private void transactAll(MoneyTracker tracker, Transactor transactor, Message message) {
		for (Map.Entry<OfflinePlayer, Counter> entry : tracker.entrySet()) {
			OfflinePlayer customer = entry.getKey();
			Counter counter = entry.getValue();
			if (counter.getTotalMoney().signum() <= 0)
				continue;

			transactor.transact(customer, counter.getTotalMoney().doubleValue(), Callback.of(
                    result -> Mailman.notify(customer, LangUtils.getMessage(message,
							new Replacement(Placeholder.AMOUNT, counter::getTotalMoney),
							new Replacement(Placeholder.NUMBER_OF_ACCOUNTS, counter::getNumberOfPayments)
					)),
					error -> Mailman.notify(customer, LangUtils.getMessage(Message.ERROR_OCCURRED,
							new Replacement(Placeholder.ERROR, error::getLocalizedMessage)
					))
			));
		}
	}

	@FunctionalInterface
	private interface Transactor {
		void transact(OfflinePlayer player, double amount, Callback<Void> callback);
	}

	private static class MoneyTracker extends HashMap<OfflinePlayer, Counter> {
		private Counter get(OfflinePlayer key) {
			super.putIfAbsent(key, new Counter());
			return super.get(key);
		}
	}

	private static class Counter extends Pair<BigDecimal, Integer> {
		private Counter() {
			super(BigDecimal.ZERO, 0);
		}
		private void add(double value) {
			add(BigDecimal.valueOf(value));
		}
		private void add(BigDecimal value) {
			super.setFirst(getTotalMoney().add(value));
			super.setSecond(getNumberOfPayments() + 1);
		}
		private BigDecimal getTotalMoney() { return super.getFirst(); }
		private int getNumberOfPayments() { return super.getSecond(); }
	}
}
