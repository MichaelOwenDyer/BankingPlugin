package com.monst.bankingplugin.utils;

import java.util.Arrays;
import java.util.List;

import com.monst.bankingplugin.config.Config;

public class AccountConfig {
	
	public static final List<String> FIELDS = Arrays.asList("interest-rate", "multipliers", "initial-interest-delay",
			"count-interest-delay-offline", "allowed-offline-payouts",
			"allowed-offline-payouts-before-multiplier-reset", "offline-multiplier-behavior",
			"withdrawal-multiplier-behavior", "account-creation-price", "reimburse-account-creation", "min-balance",
			"low-balance-fee");

	private double interestRate;
	private List<Integer> multipliers;
	private int initialInterestDelay;
	private boolean countInterestDelayOffline;
	private int allowedOfflinePayouts;
	private int allowedOfflineBeforeReset;
	private int offlineMultiplierBehavior;
	private int withdrawalMultiplierBehavior;
	private double accountCreationPrice;
	private boolean reimburseAccountCreation;
	private double minBalance;
	private double lowBalanceFee;

	public AccountConfig() {
		this(Config.interestRate.getKey(),
				Config.interestMultipliers.getKey(),
				Config.initialInterestDelay.getKey(),
				Config.countInterestDelayOffline.getKey(),
				Config.allowedOfflinePayouts.getKey(),
				Config.allowedOfflineBeforeMultiplierReset.getKey(),
				Config.offlineMultiplierBehavior.getKey(),
				Config.withdrawalMultiplierBehavior.getKey(),
				Config.creationPriceAccount.getKey(),
				Config.reimburseAccountCreation.getKey(),
				Config.minBalance.getKey(),
				Config.lowBalanceFee.getKey());
	}

	public AccountConfig(double interestRate, List<Integer> multipliers, int initialInterestDelay,
			boolean countInterestDelayOffline, int allowedOffline, int allowedOfflineBeforeReset,
			int offlineMultiplierBehavior, int withdrawalMultiplierBehavior, double accountCreationPrice,
			boolean reimburseAccountCreation, double minBalance, double lowBalanceFee) {

		this.interestRate = interestRate;
		this.multipliers = multipliers;
		this.initialInterestDelay = initialInterestDelay;
		this.countInterestDelayOffline = countInterestDelayOffline;
		this.allowedOfflinePayouts = allowedOffline;
		this.allowedOfflineBeforeReset = allowedOfflineBeforeReset;
		this.offlineMultiplierBehavior = offlineMultiplierBehavior;
		this.withdrawalMultiplierBehavior = withdrawalMultiplierBehavior;
		this.accountCreationPrice = accountCreationPrice;
		this.reimburseAccountCreation = reimburseAccountCreation;
		this.minBalance = minBalance;
		this.lowBalanceFee = lowBalanceFee;
	}

	public double getInterestRate() {
		return interestRate;
	}

	public List<Integer> getMultipliers() {
		return multipliers;
	}

	public int getInitialInterestDelay() {
		return initialInterestDelay;
	}

	public boolean isCountInterestDelayOffline() {
		return countInterestDelayOffline;
	}

	public int getAllowedOfflinePayouts() {
		return allowedOfflinePayouts;
	}

	public int getAllowedOfflineBeforeReset() {
		return allowedOfflineBeforeReset;
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

	public boolean isReimburseAccountCreation() {
		return reimburseAccountCreation;
	}

	public double getMinBalance() {
		return minBalance;
	}

	public double getLowBalanceFee() {
		return lowBalanceFee;
	}

	public void setInterestRate(double interestRate) {
		this.interestRate = interestRate;
	}

	public void setMultipliers(List<Integer> multipliers) {
		this.multipliers = multipliers;
	}

	public void setInitialInterestDelay(int initialInterestDelay) {
		this.initialInterestDelay = initialInterestDelay;
	}

	public void setCountInterestDelayOffline(boolean countInterestDelayOffline) {
		this.countInterestDelayOffline = countInterestDelayOffline;
	}

	public void setAllowedOfflinePayouts(int allowedOffline) {
		this.allowedOfflinePayouts = allowedOffline;
	}

	public void setAllowedOfflineBeforeReset(int allowedOfflineBeforeReset) {
		this.allowedOfflineBeforeReset = allowedOfflineBeforeReset;
	}

	public void setOfflineMultiplierBehavior(int offlineMultiplierBehavior) {
		this.offlineMultiplierBehavior = offlineMultiplierBehavior;
	}

	public void setWithdrawalMultiplierBehavior(int withdrawalMultiplierBehavior) {
		this.withdrawalMultiplierBehavior = withdrawalMultiplierBehavior;
	}

	public void setAccountCreationPrice(double accountCreationPrice) {
		this.accountCreationPrice = accountCreationPrice;
	}

	public void setReimburseAccountCreation(boolean reimburseAccountCreation) {
		this.reimburseAccountCreation = reimburseAccountCreation;
	}

	public void setMinBalance(double minBalance) {
		this.minBalance = minBalance;
	}

	public void setLowBalanceFee(double lowBalanceFee) {
		this.lowBalanceFee = lowBalanceFee;
	}

	public double getInterestRateOrDefault() {
		return Config.interestRate.getValue() ? interestRate : Config.interestRate.getKey();
	}

	public List<Integer> getMultipliersOrDefault() {
		return Config.interestMultipliers.getValue() ? multipliers : Config.interestMultipliers.getKey();
	}

	public int getInitialInterestDelayOrDefault() {
		return Config.initialInterestDelay.getValue() ? initialInterestDelay : Config.initialInterestDelay.getKey();
	}

	public boolean isCountInterestDelayOfflineOrDefault() {
		return Config.countInterestDelayOffline.getValue() ? countInterestDelayOffline : Config.countInterestDelayOffline.getKey();
	}

	public int getAllowedOfflinePayoutsOrDefault() {
		return Config.allowedOfflinePayouts.getValue() ? allowedOfflinePayouts : Config.allowedOfflinePayouts.getKey();
	}

	public int getAllowedOfflineBeforeResetOrDefault() {
		return Config.allowedOfflineBeforeMultiplierReset.getValue() ? allowedOfflineBeforeReset : Config.allowedOfflineBeforeMultiplierReset.getKey();
	}

	public int getOfflineMultiplierBehaviorOrDefault() {
		return Config.offlineMultiplierBehavior.getValue() ? offlineMultiplierBehavior : Config.offlineMultiplierBehavior.getKey();
	}

	public int getWithdrawalMultiplierBehaviorOrDefault() {
		return Config.withdrawalMultiplierBehavior.getValue() ? withdrawalMultiplierBehavior : Config.withdrawalMultiplierBehavior.getKey();
	}

	public double getAccountCreationPriceOrDefault() {
		return Config.creationPriceAccount.getValue() ? accountCreationPrice : Config.creationPriceAccount.getKey();
	}

	public boolean isReimburseAccountCreationOrDefault() {
		return Config.reimburseAccountCreation.getValue() ? reimburseAccountCreation : Config.reimburseAccountCreation.getKey();
	}

	public double getMinBalanceOrDefault() {
		return Config.minBalance.getValue() ? minBalance : Config.minBalance.getKey();
	}

	public double getLowBalanceFeeOrDefault() {
		return Config.lowBalanceFee.getValue() ? lowBalanceFee : Config.lowBalanceFee.getKey();
	}

}
