package com.monst.bankingplugin.utils;

import com.monst.bankingplugin.config.Config;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
	private boolean payOnLowBalance;
	private int playerAccountLimit;

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
				Config.lowBalanceFee.getValue(),
				Config.payOnLowBalance.getValue(),
				Config.playerBankAccountLimit.getValue()
				);
	}

	public AccountConfig(double interestRate, List<Integer> multipliers, int initialInterestDelay,
			boolean countInterestDelayOffline, int allowedOffline, int allowedOfflineBeforeReset,
			int offlineMultiplierBehavior, int withdrawalMultiplierBehavior, double accountCreationPrice,
			boolean reimburseAccountCreation, double minBalance, double lowBalanceFee, boolean payOnLowBalance, int playerAccountLimit) {

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
		this.payOnLowBalance = payOnLowBalance;
		this.playerAccountLimit = playerAccountLimit;
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
		case OFFLINE_MULTIPLIER_BEHAVIOR:
			return Config.offlineMultiplierBehavior.getKey();
		case WITHDRAWAL_MULTIPLIER_BEHAVIOR:
			return Config.withdrawalMultiplierBehavior.getKey();
		case ACCOUNT_CREATION_PRICE:
			return Config.creationPriceAccount.getKey();
		case REIMBURSE_ACCOUNT_CREATION:
			return Config.reimburseAccountCreation.getKey();
		case MINIMUM_BALANCE:
			return Config.minBalance.getKey();
		case LOW_BALANCE_FEE:
			return Config.lowBalanceFee.getKey();
		case PLAYER_ACCOUNT_LIMIT:
			return Config.playerBankAccountLimit.getKey();
		default:
			return false;

		}
	}

	public Object getField(Field field) {
		switch (field) {

		case INTEREST_RATE:
			return getInterestRate(false);
		case MULTIPLIERS:
			return getMultipliers(false);
		case INITIAL_INTEREST_DELAY:
			return getInitialInterestDelay(false);
		case COUNT_INTEREST_DELAY_OFFLINE:
			return isCountInterestDelayOffline(false);
		case ALLOWED_OFFLINE_PAYOUTS:
			return getAllowedOfflinePayouts(false);
		case ALLOWED_OFFLINE_PAYOUTS_BEFORE_MULTIPLIER_RESET:
			return getAllowedOfflineBeforeReset(false);
		case OFFLINE_MULTIPLIER_BEHAVIOR:
			return getOfflineMultiplierBehavior(false);
		case WITHDRAWAL_MULTIPLIER_BEHAVIOR:
			return getWithdrawalMultiplierBehavior(false);
		case ACCOUNT_CREATION_PRICE:
			return getAccountCreationPrice(false);
		case REIMBURSE_ACCOUNT_CREATION:
			return isReimburseAccountCreation(false);
		case MINIMUM_BALANCE:
			return getMinBalance(false);
		case LOW_BALANCE_FEE:
			return getLowBalanceFee(false);
		case PAY_ON_LOW_BALANCE:
			return isPayOnLowBalance(false);
		case PLAYER_ACCOUNT_LIMIT:
			return getPlayerAccountLimit(false);
		default:
			return null;
		}

	}
	
	public boolean setField(Field field, String s) throws NumberFormatException {
		
		if (!isOverrideAllowed(field))
			return false;
		
		switch (field) {
		
		case INTEREST_RATE:
			interestRate = Double.parseDouble(s.replace(",", ""));
			break;
		case MULTIPLIERS:
			multipliers = Arrays.stream(Utils.removePunctuation(s).split(" ")).filter(string -> !string.isEmpty())
					.map(Integer::parseInt).collect(Collectors.toList());
			break;
		case INITIAL_INTEREST_DELAY:
			initialInterestDelay = Integer.parseInt(s);
			break;
		case COUNT_INTEREST_DELAY_OFFLINE:
			if (s.equalsIgnoreCase("true") || s.equalsIgnoreCase("false"))
				countInterestDelayOffline = Boolean.parseBoolean(s);
			else
				throw new NumberFormatException();
			break;
		case ALLOWED_OFFLINE_PAYOUTS:
			allowedOfflinePayouts = Integer.parseInt(s);
			break;
		case ALLOWED_OFFLINE_PAYOUTS_BEFORE_MULTIPLIER_RESET:
			allowedOfflineBeforeReset = Integer.parseInt(s);
			break;
		case OFFLINE_MULTIPLIER_BEHAVIOR:
			offlineMultiplierBehavior = Integer.parseInt(s);
			break;
		case WITHDRAWAL_MULTIPLIER_BEHAVIOR:
			withdrawalMultiplierBehavior = Integer.parseInt(s);
			break;
		case ACCOUNT_CREATION_PRICE:
			accountCreationPrice = Double.parseDouble(s.replace(",", ""));
			break;
		case REIMBURSE_ACCOUNT_CREATION:
			if (s.equalsIgnoreCase("true") || s.equalsIgnoreCase("false"))
				reimburseAccountCreation = Boolean.parseBoolean(s);
			else
				throw new NumberFormatException();
			break;
		case MINIMUM_BALANCE:
			minBalance = Double.parseDouble(s.replace(",", ""));
			break;
		case LOW_BALANCE_FEE:
			lowBalanceFee = Double.parseDouble(s.replace(",", ""));
			break;
		case PAY_ON_LOW_BALANCE:
			if (s.equalsIgnoreCase("true") || s.equalsIgnoreCase("false"))
				payOnLowBalance = Boolean.parseBoolean(s);
			else
				throw new NumberFormatException();
			break;
		case PLAYER_ACCOUNT_LIMIT:
			playerAccountLimit = Integer.parseInt(s);
			break;
		default:
			return false;
		}
		return true;
	}

	public double getInterestRate(boolean ignoreConfig) {
		if (ignoreConfig)
			return interestRate;
		else
			return Config.interestRate.getKey() ? interestRate : Config.interestRate.getValue();
	}

	public List<Integer> getMultipliers(boolean ignoreConfig) {
		if (ignoreConfig)
			return multipliers;
		else
			return Config.interestMultipliers.getKey() ? multipliers : Config.interestMultipliers.getValue();
	}

	public int getInitialInterestDelay(boolean ignoreConfig) {
		if (ignoreConfig)
			return initialInterestDelay;
		else
			return Config.initialInterestDelay.getKey() ? initialInterestDelay : Config.initialInterestDelay.getValue();
	}

	public boolean isCountInterestDelayOffline(boolean ignoreConfig) {
		if (ignoreConfig)
			return countInterestDelayOffline;
		else
			return Config.countInterestDelayOffline.getKey() ? countInterestDelayOffline : Config.countInterestDelayOffline.getValue();
	}

	public int getAllowedOfflinePayouts(boolean ignoreConfig) {
		if (ignoreConfig)
			return allowedOfflinePayouts;
		else
			return Config.allowedOfflinePayouts.getKey() ? allowedOfflinePayouts : Config.allowedOfflinePayouts.getValue();
	}

	public int getAllowedOfflineBeforeReset(boolean ignoreConfig) {
		if (ignoreConfig)
			return allowedOfflineBeforeReset;
		else
			return Config.allowedOfflineBeforeMultiplierReset.getKey() ? allowedOfflineBeforeReset : Config.allowedOfflineBeforeMultiplierReset.getValue();
	}

	public int getOfflineMultiplierBehavior(boolean ignoreConfig) {
		if (ignoreConfig)
			return offlineMultiplierBehavior;
		else
			return Config.offlineMultiplierBehavior.getKey() ? offlineMultiplierBehavior : Config.offlineMultiplierBehavior.getValue();
	}

	public int getWithdrawalMultiplierBehavior(boolean ignoreConfig) {
		if (ignoreConfig)
			return withdrawalMultiplierBehavior;
		else
			return Config.withdrawalMultiplierBehavior.getKey() ? withdrawalMultiplierBehavior : Config.withdrawalMultiplierBehavior.getValue();
	}

	public double getAccountCreationPrice(boolean ignoreConfig) {
		if (ignoreConfig)
			return accountCreationPrice;
		else
			return Config.creationPriceAccount.getKey() ? accountCreationPrice : Config.creationPriceAccount.getValue();
	}

	public boolean isReimburseAccountCreation(boolean ignoreConfig) {
		if (ignoreConfig)
			return reimburseAccountCreation;
		else
			return Config.reimburseAccountCreation.getKey() ? reimburseAccountCreation : Config.reimburseAccountCreation.getValue();
	}

	public double getMinBalance(boolean ignoreConfig) {
		if (ignoreConfig)
			return minBalance;
		else
			return Config.minBalance.getKey() ? minBalance : Config.minBalance.getValue();
	}

	public double getLowBalanceFee(boolean ignoreConfig) {
		if (ignoreConfig)
			return lowBalanceFee;
		else
			return Config.lowBalanceFee.getKey() ? lowBalanceFee : Config.lowBalanceFee.getValue();
	}

	public boolean isPayOnLowBalance(boolean ignoreConfig) {
		if (ignoreConfig)
			return payOnLowBalance;
		else
			return Config.payOnLowBalance.getKey() ? payOnLowBalance : Config.payOnLowBalance.getValue();
	}

	public int getPlayerAccountLimit(boolean ignoreConfig) {
		if (ignoreConfig)
			return playerAccountLimit;
		else
			return Config.playerBankAccountLimit.getKey() ? playerAccountLimit : Config.playerBankAccountLimit.getValue();
	}

	@Override
	public String toString() {
		return Arrays.stream(Field.values()).map(field -> field.getName() + ": " + getField(field)
				+ " (Overrideable: " + isOverrideAllowed(field) + ")").collect(Collectors.joining("\n"));
	}
	
	public enum Field {

		INTEREST_RATE ("interest-rate", 0), 
		MULTIPLIERS ("multipliers", 3), 
		INITIAL_INTEREST_DELAY ("initial-interest-delay", 1), 
		COUNT_INTEREST_DELAY_OFFLINE ("count-interest-delay-offline", 2), 
		ALLOWED_OFFLINE_PAYOUTS ("allowed-offline-payouts", 1),
		ALLOWED_OFFLINE_PAYOUTS_BEFORE_MULTIPLIER_RESET ("allowed-offline-payouts-before-multiplier-reset", 1),
		OFFLINE_MULTIPLIER_BEHAVIOR ("offline-multiplier-behavior", 1), 
		WITHDRAWAL_MULTIPLIER_BEHAVIOR ("withdrawal-multiplier-behavior", 1),
		ACCOUNT_CREATION_PRICE ("account-creation-price", 0), 
		REIMBURSE_ACCOUNT_CREATION ("reimburse-account-creation", 2), 
		MINIMUM_BALANCE ("min-balance", 0), 
		LOW_BALANCE_FEE ("low-balance-fee", 0),
		PAY_ON_LOW_BALANCE ("pay-on-low-balance", 2),
		PLAYER_ACCOUNT_LIMIT ("player-account-limit", 1);
		
		private final String name;
		private final int dataType; // double: 0, integer: 1, boolean: 2, list: 3

		Field(String name, int dataType) {
			this.name = name;
			this.dataType = dataType;
		}

		public String getName() {
			return name;
		}

		public int getDataType() {
			return dataType;
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
