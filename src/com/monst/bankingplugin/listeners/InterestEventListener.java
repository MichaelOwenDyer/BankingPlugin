package com.monst.bankingplugin.listeners;

import com.monst.bankingplugin.Account;
import com.monst.bankingplugin.Bank;
import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.events.control.InterestEvent;
import com.monst.bankingplugin.utils.*;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

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
	private final BankUtils bankUtils;

	public InterestEventListener(BankingPlugin plugin) {
		this.plugin = plugin;
		this.accountUtils = plugin.getAccountUtils();
		this.bankUtils = plugin.getBankUtils();
	}

	/**
	 * Adds some money to the running sum for each player, and increments the associated counter.
	 * @param counter Which {@link HashMap} to add the {@link BigDecimal} to.
	 * @param player The {@link OfflinePlayer} the money either belongs to or must be paid by.
	 * @param toAdd The {@link BigDecimal} describing the amount of money.
	 */
	private static void add(Map<OfflinePlayer, AbstractMap.SimpleEntry<BigDecimal, Integer>> counter,
							OfflinePlayer player, BigDecimal toAdd) {

		AbstractMap.SimpleEntry<BigDecimal, Integer> current =
				counter.getOrDefault(player, new AbstractMap.SimpleEntry<>(BigDecimal.ZERO, 0));

		counter.put(player, new AbstractMap.SimpleEntry<>(current.getKey().add(toAdd), current.getValue() + 1));
	}

	@EventHandler(ignoreCancelled = true)
	public void onInterestEvent(InterestEvent e) {
		
		new BukkitRunnable() {
			@Override
			public void run() {
				plugin.debug("Interest payout event occurring now!");

				Map<OfflinePlayer, List<Account>> playerAccountMap = accountUtils.getAccountsCopy().stream()
						.collect(Collectors.groupingBy(Account::getOwner));
				Map<OfflinePlayer, List<Bank>> playerBankMap = bankUtils.getBanksCopy().stream()
						.filter(Bank::isPlayerBank)
						.collect(Collectors.groupingBy(Bank::getOwner));

				if (playerAccountMap.isEmpty())
					return;

				Map<OfflinePlayer, AbstractMap.SimpleEntry<BigDecimal, Integer>> interestReceivable = new HashMap<>(); // The amount of interest each account owner earns on how many accounts
				Map<OfflinePlayer, AbstractMap.SimpleEntry<BigDecimal, Integer>> feesPayable = new HashMap<>(); // The amount of fees each account owner must pay for how many accounts

				Map<OfflinePlayer, AbstractMap.SimpleEntry<BigDecimal, Integer>> interestPayable = new HashMap<>(); // The amount of interest each bank owner must pay for how many banks
				Map<OfflinePlayer, AbstractMap.SimpleEntry<BigDecimal, Integer>> feesReceivable = new HashMap<>(); // The amount of fees each bank owner receives as income on how many banks

				for (OfflinePlayer accountOwner : playerAccountMap.keySet()) {
					
					for (Account account : playerAccountMap.get(accountOwner)) {

						if (!account.getStatus().allowNextPayout(account.isTrustedPlayerOnline())) {
							accountUtils.addAccount(account, true);
							continue;
						}

						Set<OfflinePlayer> trustedPlayers = account.getTrustedPlayers();
						if (account.getBank().isPlayerBank())
							trustedPlayers.remove(account.getBank().getOwner());

						AccountConfig config = account.getBank().getAccountConfig();

						if (!trustedPlayers.isEmpty() && (double) config.get(AccountConfig.Field.MINIMUM_BALANCE) > 0
								&& account.getBalance().compareTo(BigDecimal.valueOf(config.get(AccountConfig.Field.MINIMUM_BALANCE))) < 0) {

							add(feesPayable, accountOwner, config.get(AccountConfig.Field.LOW_BALANCE_FEE));
							if (account.getBank().isPlayerBank())
								add(feesReceivable, account.getBank().getOwner(), config.get(AccountConfig.Field.LOW_BALANCE_FEE));

							if (Config.enableInterestLog)
								plugin.getDatabase().logInterest(account, BigDecimal.ZERO, 0,
										BigDecimal.valueOf((double) config.get(AccountConfig.Field.LOW_BALANCE_FEE) * -1), null);

							if (!(boolean) config.get(AccountConfig.Field.PAY_ON_LOW_BALANCE))
								continue;
						}

						BigDecimal baseInterest = account.getBalance()
								.multiply(BigDecimal.valueOf(config.get(AccountConfig.Field.INTEREST_RATE)))
								.setScale(2, RoundingMode.HALF_EVEN);
						BigDecimal interest = baseInterest;

						int multiplier = account.getStatus().getRealMultiplier();
						interest = interest.multiply(BigDecimal.valueOf(multiplier));

						account.getStatus().incrementMultiplier(account.isTrustedPlayerOnline());
						account.updatePrevBalance();

						if (trustedPlayers.size() == 0) {
							accountUtils.addAccount(account, true);
							continue;
						}

						BigDecimal cut = interest.divide(BigDecimal.valueOf(trustedPlayers.size()), RoundingMode.HALF_EVEN);
						for (OfflinePlayer recipient : trustedPlayers)
							add(interestReceivable, recipient, cut);

						if (account.getBank().isPlayerBank())
							add(interestPayable, account.getBank().getOwner(), interest);

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
						int numberOfAccounts = bank.getCustomerAccounts().size();

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

					if (feesReceivable.get(bankOwner).getKey().signum() == 0)
						continue;
					if (!bankOwner.hasPlayedBefore())
						continue;

					boolean isOnline = bankOwner.isOnline();
					String worldName = isOnline ? bankOwner.getPlayer().getWorld().getName() : fallbackWorldName;
					Utils.depositPlayer(bankOwner, worldName, feesReceivable.get(bankOwner).getKey().doubleValue(), new Callback<Void>(plugin) {
						@Override
						public void onResult(Void result) {
							if (isOnline) {
								int count = feesReceivable.get(bankOwner).getValue();
								bankOwner.getPlayer().sendMessage(String.format(Messages.LOW_BALANCE_FEE_EARNED,
										Utils.format(feesReceivable.get(bankOwner)),
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

					if (interestPayable.get(bankOwner).getKey().signum() == 0)
						continue;
					if (!bankOwner.hasPlayedBefore())
						continue;

					boolean isOnline = bankOwner.isOnline();
					String worldName = isOnline ? bankOwner.getPlayer().getWorld().getName() : fallbackWorldName;

					Utils.withdrawPlayer(bankOwner, worldName, interestPayable.get(bankOwner).getKey().doubleValue(), new Callback<Void>(plugin) {
						@Override
						public void onResult(Void result) {
							if (isOnline) {
								int count = interestPayable.get(bankOwner).getValue();
								bankOwner.getPlayer().sendMessage(String.format(Messages.INTEREST_PAID,
										Utils.format(interestPayable.get(bankOwner)),
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

					if (interestReceivable.get(customer).getKey().signum() == 0)
						continue;

					boolean online = customer.isOnline();
					String worldName = playerAccountMap.get(customer) != null
							? playerAccountMap.get(customer).get(0).getLocation().getWorld().getName()
							: (online ? customer.getPlayer().getWorld().getName() : fallbackWorldName);

					Utils.depositPlayer(customer, worldName, interestReceivable.get(customer).getKey().doubleValue(), new Callback<Void>(plugin) {
						@Override
						public void onResult(Void result) {
							if (online) {
								int count = interestReceivable.get(customer).getValue();
								customer.getPlayer().sendMessage(String.format(Messages.INTEREST_EARNED,
										Utils.format(interestReceivable.get(customer)),
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

					if (feesPayable.get(customer).getKey().signum() == 0)
						continue;

					boolean online = customer.isOnline();
					String worldName = playerAccountMap.get(customer) != null
							? playerAccountMap.get(customer).get(0).getLocation().getWorld().getName()
							: (online ? customer.getPlayer().getWorld().getName() : fallbackWorldName);

					Utils.withdrawPlayer(customer, worldName, feesPayable.get(customer).getKey().doubleValue(), new Callback<Void>(plugin) {
						@Override
						public void onResult(Void result) {
							if (online) {
								int count = feesPayable.get(customer).getValue();
								customer.getPlayer().sendMessage(String.format(Messages.LOW_BALANCE_FEE_PAID,
										Utils.format(feesPayable.get(customer)),
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
		}.runTaskAsynchronously(plugin);
	}
}
