package com.monst.bankingplugin.utils;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.monst.bankingplugin.config.Config;

public class AccountConfig {

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
		this(Config.interestRate.getValue(),
				Config.interestMultipliers.getValue(),
				Config.initialInterestDelay.getValue(),
				Config.countInterestDelayOffline.getValue(),
				Config.allowedOfflinePayouts.getValue(),
				Config.allowedOfflineBeforeMultiplierReset.getValue(),
				Config.offlineMultiplierBehavior.getValue(),
				Config.withdrawalMultiplierBehavior.getValue(),
				Config.creationPriceAccount.getValue(),
				Config.reimburseAccountCreation.getValue(),
				Config.minBalance.getValue(),
				Config.lowBalanceFee.getValue()
				);
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

	public static boolean isOverrideAllowed(Field field) {
		switch (field) {

		case INTEREST_RATE:
			return Config.interestRate.getKey();
		case MULTIPLIERS:
			return Config.interestMultipliers.getKey();
		case INITIAL_INTEREST_DELAY:
			return Config.initialInterestDelay.getKey();
		case COUNT_INTEREST_DELAY_OFFLINE:
			return Config.countInterestDelayOffline.getKey();
		case ALLOWED_OFFLINE_PAYOUTS:
			return Config.allowedOfflinePayouts.getKey();
		case ALLOWED_OFFLINE_PAYOUTS_BEFORE_MULTIPLIER_RESET:
			return Config.allowedOfflineBeforeMultiplierReset.getKey();
		case OFFLINE_MULTIPLAYER_BEHAVIOR:
			return Config.offlineMultiplierBehavior.getKey();
		case WITHDRAWAL_MULTIPLIER_BEHAVIOR:
			return Config.withdrawalMultiplierBehavior.getKey();
		case ACCOUNT_CREATION_PRICE:
			return Config.creationPriceAccount.getKey();
		case REIMBURSE_ACCOUNT_CREATION:
			return Config.reimburseAccountCreation.getKey();
		case MIN_BALANCE:
			return Config.minBalance.getKey();
		case LOW_BALANCE_FEE:
			return Config.lowBalanceFee.getKey();
		default:
			return false;

		}
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
		return Config.interestRate.getKey() ? interestRate : Config.interestRate.getValue();
	}

	public List<Integer> getMultipliersOrDefault() {
		return Config.interestMultipliers.getKey() ? multipliers : Config.interestMultipliers.getValue();
	}

	public int getInitialInterestDelayOrDefault() {
		return Config.initialInterestDelay.getKey() ? initialInterestDelay : Config.initialInterestDelay.getValue();
	}

	public boolean isCountInterestDelayOfflineOrDefault() {
		return Config.countInterestDelayOffline.getKey() ? countInterestDelayOffline : Config.countInterestDelayOffline.getValue();
	}

	public int getAllowedOfflinePayoutsOrDefault() {
		return Config.allowedOfflinePayouts.getKey() ? allowedOfflinePayouts : Config.allowedOfflinePayouts.getValue();
	}

	public int getAllowedOfflineBeforeResetOrDefault() {
		return Config.allowedOfflineBeforeMultiplierReset.getKey() ? allowedOfflineBeforeReset : Config.allowedOfflineBeforeMultiplierReset.getValue();
	}

	public int getOfflineMultiplierBehaviorOrDefault() {
		return Config.offlineMultiplierBehavior.getKey() ? offlineMultiplierBehavior : Config.offlineMultiplierBehavior.getValue();
	}

	public int getWithdrawalMultiplierBehaviorOrDefault() {
		return Config.withdrawalMultiplierBehavior.getKey() ? withdrawalMultiplierBehavior : Config.withdrawalMultiplierBehavior.getValue();
	}

	public double getAccountCreationPriceOrDefault() {
		return Config.creationPriceAccount.getKey() ? accountCreationPrice : Config.creationPriceAccount.getValue();
	}

	public boolean isReimburseAccountCreationOrDefault() {
		return Config.reimburseAccountCreation.getKey() ? reimburseAccountCreation : Config.reimburseAccountCreation.getValue();
	}

	public double getMinBalanceOrDefault() {
		return Config.minBalance.getKey() ? minBalance : Config.minBalance.getValue();
	}

	public double getLowBalanceFeeOrDefault() {
		return Config.lowBalanceFee.getKey() ? lowBalanceFee : Config.lowBalanceFee.getValue();
	}

	public enum Field {

		INTEREST_RATE ("interest-rate"), 
		MULTIPLIERS ("multipliers"), 
		INITIAL_INTEREST_DELAY ("initial-interest-delay"), 
		COUNT_INTEREST_DELAY_OFFLINE ("count-interest-delay-offline"), 
		ALLOWED_OFFLINE_PAYOUTS ("allowed-offline-payouts"),
		ALLOWED_OFFLINE_PAYOUTS_BEFORE_MULTIPLIER_RESET ("allowed-offline-payouts-before-multiplier-reset"),
		OFFLINE_MULTIPLAYER_BEHAVIOR ("offline-multiplier-behavior"), 
		WITHDRAWAL_MULTIPLIER_BEHAVIOR ("withdrawal-multiplier-behavior"),
		ACCOUNT_CREATION_PRICE ("account-creation-price"), 
		REIMBURSE_ACCOUNT_CREATION ("reimburse-account-creation"), 
		MIN_BALANCE ("min-balance"), 
		LOW_BALANCE_FEE ("low-balance-fee");
		
		private String name;

		Field(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public static Stream<Field> stream() {
			return Stream.of(Field.values());
		}

		public static List<String> names() {
			return stream().map(Field::getName).collect(Collectors.toList());
		}

		public static Field getByName(String name) {
			return stream().filter(field -> field.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
		}
	}
	
}
