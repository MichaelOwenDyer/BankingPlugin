package com.monst.bankingplugin.listeners;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import com.monst.bankingplugin.Account;
import com.monst.bankingplugin.Bank;
import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.events.InterestEvent;
import com.monst.bankingplugin.utils.AccountConfig;
import com.monst.bankingplugin.utils.AccountConfig.Field;
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
				
				Map<OfflinePlayer, BigDecimal> bankTotalProfit = new HashMap<>(); // The amount of profit each bank owner earns

				for (OfflinePlayer owner : playerAccounts.keySet()) {
					
					for (Account account : playerAccounts.get(owner)) {

						AccountConfig config = account.getBank().getAccountConfig();
						AccountStatus status = account.getStatus();
						if (!status.allowNextPayout(account.isTrustedPlayerOnline())) {
							plugin.getDatabase().addAccount(account, null);
							continue;
						}

						if ((double) config.getOrDefault(Field.MINIMUM_BALANCE) > 0 && account.getBalance()
								.compareTo(BigDecimal.valueOf((double) config.getOrDefault(Field.MINIMUM_BALANCE))) == -1) {
							if (!account.getBank().isAdminBank() && account.isOwner(account.getBank().getOwner()))
								continue;
							totalAccountFees.put(owner, totalAccountFees.getOrDefault(owner, BigDecimal.ZERO)
									.add(BigDecimal.valueOf((double) config.getOrDefault(Field.LOW_BALANCE_FEE))));
							accountFeeCounter.put(owner, accountFeeCounter.getOrDefault(owner, 0).intValue() + 1);
							if (!account.getBank().isAdminBank()) {
								totalBankFees.put(account.getBank().getOwner(),
									totalBankFees.getOrDefault(account.getBank().getOwner(), BigDecimal.ZERO)
												.add(BigDecimal.valueOf((double) config.getOrDefault(Field.LOW_BALANCE_FEE))));
								bankFeeCounter.put(account.getBank().getOwner(),
									bankFeeCounter.getOrDefault(account.getBank().getOwner(), 0).intValue() + 1);
							}
							if (Config.enableInterestLog) {
								plugin.getDatabase().logInterest(account, BigDecimal.ZERO, 0,
										BigDecimal.valueOf((double) config.getOrDefault(Field.LOW_BALANCE_FEE) * -1), null);
							}
							continue; // Config value for paying interest on low balance or not?
						}

						BigDecimal baseInterest = account.getBalance()
								.multiply(BigDecimal.valueOf((double) config.getOrDefault(Field.INTEREST_RATE)))
								.setScale(2, RoundingMode.HALF_EVEN);
						BigDecimal interest = baseInterest;

						int multiplier = status.getRealMultiplier();
						interest = interest.multiply(BigDecimal.valueOf(multiplier));

						status.incrementMultiplier(account.isTrustedPlayerOnline());
						account.updatePrevBalance();

						Set<OfflinePlayer> trustedPlayers = account.getTrustedPlayersCopy();
						final int payoutSplit = trustedPlayers.size();
						// TODO: Bank owners should not pay themselves interest
						for (OfflinePlayer recipient : account.getTrustedPlayersCopy()) {
							totalAccountInterest.put(recipient, totalAccountInterest.getOrDefault(recipient, BigDecimal.ZERO)
									.add(interest.divide(BigDecimal.valueOf(payoutSplit))));
							accountInterestCounter.put(recipient, accountInterestCounter.getOrDefault(recipient, 0).intValue() + 1);
						}
						if (!account.getBank().isAdminBank()) {
							totalBankInterest.put(account.getBank().getOwner(),
									totalBankInterest.getOrDefault(account.getBank().getOwner(), BigDecimal.ZERO).add(interest));
							bankInterestCounter.put(account.getBank().getOwner(),
									bankInterestCounter.getOrDefault(account.getBank().getOwner(), 0).intValue() + 1);
						}

						plugin.getDatabase().addAccount(account, null);

						if (Config.enableInterestLog) {
							plugin.getDatabase().logInterest(account, baseInterest, multiplier, interest, null);
						}

					}
					if (owner.isOnline())
						plugin.getDatabase().logLogout(owner.getPlayer(), null);
				}
				
				World fallbackWorld = playerAccounts.values().stream().flatMap(list -> list.stream())
						.collect(Collectors.toList()).get(0).getLocation().getWorld();

				for (OfflinePlayer bankOwner : playerBanks.keySet()) {
					for (Bank bank : playerBanks.get(bankOwner)) {

						BigDecimal totalValue = bank.getTotalValue();

						if (totalValue.signum() == 0)
							continue;

						double multiplier = Config.bankRevenueMultiplier;
						double gini = bank.getGiniCoefficient();
						int numberOfAccounts = bank.getCustomerAccounts().size();

						BigDecimal revenue = totalValue.multiply(BigDecimal.valueOf(multiplier))
								.multiply(BigDecimal.valueOf(gini))
								.multiply(BigDecimal.valueOf(Math.log(numberOfAccounts)));

						boolean online = bankOwner.isOnline();

						EconomyResponse r = plugin.getEconomy().depositPlayer(bankOwner, bank.getWorld().getName(), revenue.doubleValue());
						if (!r.transactionSuccess()) {
							plugin.debug("Economy transaction failed: " + r.errorMessage);
							if (online)
								bankOwner.getPlayer().sendMessage(Messages.ERROR_OCCURRED);
						} else if (online)
							bankOwner.getPlayer().sendMessage(String.format(Messages.INTEREST_EARNED,
									Utils.formatNumber(revenue), bank.getName()));
					}
				}

				for (OfflinePlayer customer : totalAccountInterest.keySet()) {

					if (totalAccountInterest.get(customer).signum() == 0)
						continue;

					World world;
					if (playerAccounts.get(customer) != null)
						world = playerAccounts.get(customer).get(0).getLocation().getWorld();
					else
						world = fallbackWorld;

					boolean online = customer.isOnline();
					EconomyResponse r = plugin.getEconomy().depositPlayer(customer, online ? customer.getPlayer().getWorld().getName() : world.getName(),
							totalAccountInterest.get(customer).doubleValue());
					if (!r.transactionSuccess()) {
						plugin.debug("Economy transaction failed: " + r.errorMessage);
						if (online)
							customer.getPlayer().sendMessage(Messages.ERROR_OCCURRED);
					} else if (online)
						customer.getPlayer()
								.sendMessage(String.format(Messages.INTEREST_EARNED,
										Utils.formatNumber(totalAccountInterest.get(customer)), accountInterestCounter.get(customer),
										accountInterestCounter.get(customer) == 1 ? "" : "s"));

				}

				for (OfflinePlayer customer : totalAccountFees.keySet()) {

					if (totalAccountFees.get(customer).signum() == 0)
						continue;

					World world;
					if (playerAccounts.get(customer) != null)
						world = playerAccounts.get(customer).get(0).getLocation().getWorld();
					else
						world = fallbackWorld;

					boolean online = customer.isOnline();
					EconomyResponse r = plugin.getEconomy().withdrawPlayer(customer, online ? customer.getPlayer().getWorld().getName() : world.getName(),
							totalAccountFees.get(customer).doubleValue());
					if (!r.transactionSuccess()) {
						plugin.debug("Economy transaction failed: " + r.errorMessage);
						if (online)
							customer.getPlayer().sendMessage(Messages.ERROR_OCCURRED);
					} else if (online)
						customer.getPlayer()
								.sendMessage(String.format(Messages.LOW_BALANCE_FEE_PAID,
										Utils.formatNumber(totalAccountFees.get(customer)), accountFeeCounter.get(customer),
										accountFeeCounter.get(customer) == 1 ? "" : "s"));
				}
				
				for (OfflinePlayer bankOwner : totalBankInterest.keySet()) {
					
					if (totalBankInterest.get(bankOwner).signum() == 0)
						continue;

					boolean isOnline = bankOwner.isOnline();
					World world = isOnline ? bankOwner.getPlayer().getLocation().getWorld() : fallbackWorld;

					EconomyResponse r2 = plugin.getEconomy().withdrawPlayer(bankOwner, world.getName(),
							totalBankInterest.get(bankOwner).doubleValue());
					if (!r2.transactionSuccess()) {
						plugin.debug("Economy transaction failed: " + r2.errorMessage);
						if (isOnline)
							bankOwner.getPlayer().sendMessage(Messages.ERROR_OCCURRED);
					} else if (isOnline)
						bankOwner.getPlayer()
								.sendMessage(String.format(Messages.INTEREST_PAID,
										Utils.formatNumber(totalBankInterest.get(bankOwner)),
										bankInterestCounter.get(bankOwner),
										bankInterestCounter.get(bankOwner) == 1 ? "" : "s"));

				}
				
				for (OfflinePlayer bankOwner : totalBankFees.keySet()) {
					if (totalBankFees.get(bankOwner).signum() == 0)
						continue;

					World world = fallbackWorld;

					boolean online = bankOwner.isOnline();
					EconomyResponse r = plugin.getEconomy().depositPlayer(bankOwner, online ? bankOwner.getPlayer().getWorld().getName() : world.getName(),
							totalBankFees.get(bankOwner).doubleValue());
					if (!r.transactionSuccess()) {
						plugin.debug("Economy transaction failed: " + r.errorMessage);
						if (online)
							bankOwner.getPlayer().sendMessage(Messages.ERROR_OCCURRED);
					} else if (online)
						bankOwner.getPlayer()
								.sendMessage(String.format(Messages.LOW_BALANCE_FEE_EARNED,
										Utils.formatNumber(totalBankFees.get(bankOwner)), bankFeeCounter.get(bankOwner),
										bankFeeCounter.get(bankOwner) == 1 ? "" : "s"));

				}

			}
		}.runTaskAsynchronously(plugin);
	}
}
