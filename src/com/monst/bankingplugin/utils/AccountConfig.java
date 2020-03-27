package com.monst.bankingplugin.utils;

import java.util.List;

import com.monst.bankingplugin.config.Config;

public class AccountConfig {

	private int interestDelay;
	private int allowedOffline;
	private int allowedOfflineBeforeReset;
	private double baseInterestRate;
	private List<Integer> multipliers;
	private boolean enableMultipliers;
	private boolean countInterestDelayOffline;
	private boolean reimburseAccountCreation;
	private int offlineMultiplierBehavior;
	private int withdrawalMultiplierBehavior;
	private double accountCreationPrice;
	private double minBalance;

	public AccountConfig() {
		this(Config.interestDelayPeriod, Config.allowedOfflinePayouts, Config.allowedOfflineBeforeMultiplierReset,
				Config.baseInterestRate, Config.interestMultipliers, Config.enableInterestMultipliers, 
				Config.interestDelayCountWhileOffline, Config.reimburseAccountCreation,Config.offlineMultiplierBehavior, 
				Config.withdrawalMultiplierBehavior, Config.creationPriceAccount, Config.minBalance);
	}

	public AccountConfig(int interestDelay, int allowedOffline, int allowedOfflineBeforeReset,
			double baseInterestRate, List<Integer> multipliers, boolean enableMultipliers,
			boolean countInterestDelayOffline, boolean reimburseAccountCreation, int offlineMultiplierBehavior,
			int withdrawalMultiplierBehavior, double accountCreationPrice, double minBalance) {
		this.interestDelay = interestDelay;
		this.allowedOffline = allowedOffline;
		this.allowedOfflineBeforeReset = allowedOfflineBeforeReset;
		this.baseInterestRate = baseInterestRate;
		this.multipliers = multipliers;
		this.enableMultipliers = enableMultipliers;
		this.countInterestDelayOffline = countInterestDelayOffline;
		this.reimburseAccountCreation = reimburseAccountCreation;
		this.offlineMultiplierBehavior = offlineMultiplierBehavior;
		this.withdrawalMultiplierBehavior = withdrawalMultiplierBehavior;
		this.accountCreationPrice = accountCreationPrice;
		this.minBalance = minBalance;
	}

	public int getInterestDelay() {
		return interestDelay;
	}

	public int getAllowedOffline() {
		return allowedOffline;
	}

	public int getAllowedOfflineBeforeReset() {
		return allowedOfflineBeforeReset;
	}

	public double getBaseInterestRate() {
		return baseInterestRate;
	}

	public List<Integer> getMultipliers() {
		return multipliers;
	}

	public boolean multipliersEnabled() {
		return enableMultipliers;
	}

	public boolean countsInterestDelayOffline() {
		return countInterestDelayOffline;
	}

	public boolean reimbursesAccountCreation() {
		return reimburseAccountCreation;
	}

	public int getOfflineMultiplierBehavior() {
		return offlineMultiplierBehavior;
	}

	public int getWithdrawalMultiplierBehavior() {
		return withdrawalMultiplierBehavior;
	}

	public double getAccountCreationPrice() {
		return accountCreationPrice;
	}

	public double getMinBalance() {
		return minBalance;
	}

}
