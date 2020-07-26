package com.monst.bankingplugin.listeners;

import com.monst.bankingplugin.Account;
import com.monst.bankingplugin.Bank;
import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.events.InterestEvent;
import com.monst.bankingplugin.utils.*;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

public class InterestEventListener implements Listener {
	
	private final BankingPlugin plugin;
	private final AccountUtils accountUtils;
	private final BankUtils bankUtils;

	public InterestEventListener(BankingPlugin plugin) {
		this.plugin = plugin;
		this.accountUtils = plugin.getAccountUtils();
		this.bankUtils = plugin.getBankUtils();
	}

	@EventHandler
	public void onInterestEvent(InterestEvent e) {
		
		new BukkitRunnable() {
			@Override
			public void run() {
				plugin.debug("Interest payout event occurring now!");

				Map<OfflinePlayer, List<Account>> playerAccounts = accountUtils.getAccountsCopy().stream()
						.collect(Collectors.groupingBy(Account::getOwner));
				Map<OfflinePlayer, List<Bank>> playerBanks = bankUtils.getBanksCopy().stream()
						.filter(bank -> !bank.isAdminBank()).collect(Collectors.groupingBy(Bank::getOwner));

				if (playerAccounts.isEmpty())
					return;

				Map<OfflinePlayer, BigDecimal> totalAccountInterest = new HashMap<>(); // The amount of interest each account owner earns
				Map<OfflinePlayer, BigDecimal> totalAccountFees = new HashMap<>(); // The amount of fees each account owner must pay
				Map<OfflinePlayer, Integer> accountInterestCounter = new HashMap<>(); // The number of accounts each account owner is earning interest on
				Map<OfflinePlayer, Integer> accountFeeCounter = new HashMap<>(); // The number of accounts each account owner must pay fees for

				Map<OfflinePlayer, BigDecimal> totalBankInterest = new HashMap<>(); // The amount of interest each bank owner must pay
				Map<OfflinePlayer, BigDecimal> totalBankFees = new HashMap<>(); // The amount of fees each bank owner receives as income
				Map<OfflinePlayer, Integer> bankInterestCounter = new HashMap<>(); // The number of accounts each bank owner must pay interest on
				Map<OfflinePlayer, Integer> bankFeeCounter = new HashMap<>(); // The number of fees each bank owner receives as income
				
				for (OfflinePlayer accountOwner : playerAccounts.keySet()) {
					
					for (Account account : playerAccounts.get(accountOwner)) {

						AccountConfig config = account.getBank().getAccountConfig();
						AccountStatus status = account.getStatus();
						if (!status.allowNextPayout(account.isTrustedPlayerOnline())) {
							accountUtils.addAccount(account, true);
							continue;
						}
						
						Set<OfflinePlayer> trustedPlayers = account.getTrustedPlayers();
						if (!account.getBank().isAdminBank())
							trustedPlayers.remove(account.getBank().getOwner());

						if (!trustedPlayers.isEmpty() && config.getMinBalance(false) > 0
								&& account.getBalance().compareTo(BigDecimal.valueOf(config.getMinBalance(false))) < 0) {

							totalAccountFees.put(accountOwner, totalAccountFees.getOrDefault(accountOwner, BigDecimal.ZERO)
									.add(BigDecimal.valueOf(config.getLowBalanceFee(false))));
							accountFeeCounter.put(accountOwner, accountFeeCounter.getOrDefault(accountOwner, 0) + 1);
							if (!account.getBank().isAdminBank()) {
								totalBankFees.put(account.getBank().getOwner(),
									totalBankFees.getOrDefault(account.getBank().getOwner(), BigDecimal.ZERO)
												.add(BigDecimal.valueOf(config.getLowBalanceFee(false))));
								bankFeeCounter.put(account.getBank().getOwner(),
										bankFeeCounter.getOrDefault(account.getBank().getOwner(), 0) + 1);
							}
							if (Config.enableInterestLog) {
								plugin.getDatabase().logInterest(account, BigDecimal.ZERO, 0,
										BigDecimal.valueOf(config.getLowBalanceFee(false) * -1), null);
							}
							if (!config.isPayOnLowBalance(false))
								continue;
						}

						BigDecimal baseInterest = account.getBalance()
								.multiply(BigDecimal.valueOf(config.getInterestRate(false)))
								.setScale(2, RoundingMode.HALF_EVEN);
						BigDecimal interest = baseInterest;

						int multiplier = status.getRealMultiplier();
						interest = interest.multiply(BigDecimal.valueOf(multiplier));

						status.incrementMultiplier(account.isTrustedPlayerOnline());
						account.updatePrevBalance();
						
						final int payoutSplit = trustedPlayers.size();
						for (OfflinePlayer recipient : trustedPlayers) {
							totalAccountInterest.put(recipient, totalAccountInterest.getOrDefault(recipient, BigDecimal.ZERO)
									.add(interest.divide(BigDecimal.valueOf(payoutSplit), RoundingMode.HALF_EVEN)));
							accountInterestCounter.put(recipient, accountInterestCounter.getOrDefault(recipient, 0) + 1);
						}
						if (payoutSplit != 0 && !account.getBank().isAdminBank()) {
							totalBankInterest.put(account.getBank().getOwner(),
									totalBankInterest.getOrDefault(account.getBank().getOwner(), BigDecimal.ZERO)
											.add(interest));
							bankInterestCounter.put(account.getBank().getOwner(),
									bankInterestCounter.getOrDefault(account.getBank().getOwner(), 0) + 1);
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
				for (OfflinePlayer bankOwner : playerBanks.keySet()) {

					for (Bank bank : playerBanks.get(bankOwner)) {
						if (!bankOwner.hasPlayedBefore())
							continue;

						BigDecimal totalValue = bank.getTotalValue();

						if (totalValue.signum() == 0)
							continue;

						double multiplier = Config.bankRevenueMultiplier;
						double gini = Utils.getGiniCoefficient(bank);
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
											Utils.formatNumber(revenue), bank.getName()));
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

				String fallbackWorldName = playerAccounts.values().stream().flatMap(Collection::stream)
						.collect(Collectors.toList()).get(0).getLocation().getWorld().getName();

				// Bank owners receive low balance fees
				for (OfflinePlayer bankOwner : totalBankFees.keySet()) {

					if (totalBankFees.get(bankOwner).signum() == 0)
						continue;
					if (!bankOwner.hasPlayedBefore())
						continue;

					boolean isOnline = bankOwner.isOnline();
					String worldName = isOnline ? bankOwner.getPlayer().getWorld().getName() : fallbackWorldName;
					Utils.depositPlayer(bankOwner, worldName, totalBankFees.get(bankOwner).doubleValue(), new Callback<Void>(plugin) {
						@Override
						public void onResult(Void result) {
							if (isOnline)
								bankOwner.getPlayer().sendMessage(String.format(Messages.LOW_BALANCE_FEE_EARNED,
												Utils.formatNumber(totalBankFees.get(bankOwner)),
												bankFeeCounter.get(bankOwner),
												bankFeeCounter.get(bankOwner) == 1 ? "" : "s"));
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
				for (OfflinePlayer bankOwner : totalBankInterest.keySet()) {

					if (totalBankInterest.get(bankOwner).signum() == 0)
						continue;
					if (!bankOwner.hasPlayedBefore())
						continue;

					boolean isOnline = bankOwner.isOnline();
					String worldName = isOnline ? bankOwner.getPlayer().getWorld().getName() : fallbackWorldName;

					Utils.withdrawPlayer(bankOwner, worldName, totalBankInterest.get(bankOwner).doubleValue(), new Callback<Void>(plugin) {
						@Override
						public void onResult(Void result) {
							if (isOnline)
								bankOwner.getPlayer().sendMessage(String.format(Messages.INTEREST_PAID,
												Utils.formatNumber(totalBankInterest.get(bankOwner)),
												bankInterestCounter.get(bankOwner),
												bankInterestCounter.get(bankOwner) == 1 ? "" : "s"));
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
				for (OfflinePlayer customer : totalAccountInterest.keySet()) {

					if (totalAccountInterest.get(customer).signum() == 0)
						continue;

					boolean online = customer.isOnline();
					String worldName = playerAccounts.get(customer) != null
							? playerAccounts.get(customer).get(0).getLocation().getWorld().getName()
							: (online ? customer.getPlayer().getWorld().getName() : fallbackWorldName);

					Utils.depositPlayer(customer, worldName, totalAccountInterest.get(customer).doubleValue(), new Callback<Void>(plugin) {
						@Override
						public void onResult(Void result) {
							if (online)
								customer.getPlayer().sendMessage(String.format(Messages.INTEREST_EARNED,
												Utils.formatNumber(totalAccountInterest.get(customer)),
												accountInterestCounter.get(customer),
												accountInterestCounter.get(customer) == 1 ? "" : "s"));
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
				for (OfflinePlayer customer : totalAccountFees.keySet()) {

					if (totalAccountFees.get(customer).signum() == 0)
						continue;

					boolean online = customer.isOnline();
					String worldName = playerAccounts.get(customer) != null
							? playerAccounts.get(customer).get(0).getLocation().getWorld().getName()
							: (online ? customer.getPlayer().getWorld().getName() : fallbackWorldName);

					Utils.withdrawPlayer(customer, worldName, totalAccountFees.get(customer).doubleValue(), new Callback<Void>(plugin) {
						@Override
						public void onResult(Void result) {
							if (online)
								customer.getPlayer().sendMessage(String.format(Messages.LOW_BALANCE_FEE_PAID,
												Utils.formatNumber(totalAccountFees.get(customer)),
												accountFeeCounter.get(customer),
												accountFeeCounter.get(customer) == 1 ? "" : "s"));
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
