package com.monst.bankingplugin.utils;

import java.util.Arrays;
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
		case MINIMUM_BALANCE:
			return Config.minBalance.getKey();
		case LOW_BALANCE_FEE:
			return Config.lowBalanceFee.getKey();
		default:
			return false;

		}
	}

	public Object getOrDefault(Field field) {
		switch (field) {

		case INTEREST_RATE:
			return Config.interestRate.getKey() ? interestRate : Config.interestRate.getValue();
		case MULTIPLIERS:
			return Config.interestMultipliers.getKey() ? multipliers : Config.interestMultipliers.getValue();
		case INITIAL_INTEREST_DELAY:
			return Config.initialInterestDelay.getKey() ? initialInterestDelay : Config.initialInterestDelay.getValue();
		case COUNT_INTEREST_DELAY_OFFLINE:
			return Config.countInterestDelayOffline.getKey() ? countInterestDelayOffline : Config.countInterestDelayOffline.getValue();
		case ALLOWED_OFFLINE_PAYOUTS:
			return Config.allowedOfflinePayouts.getKey() ? allowedOfflinePayouts : Config.allowedOfflinePayouts.getValue();
		case ALLOWED_OFFLINE_PAYOUTS_BEFORE_MULTIPLIER_RESET:
			return Config.allowedOfflineBeforeMultiplierReset.getKey() ? allowedOfflineBeforeReset : Config.allowedOfflineBeforeMultiplierReset.getValue();
		case OFFLINE_MULTIPLAYER_BEHAVIOR:
			return Config.offlineMultiplierBehavior.getKey() ? offlineMultiplierBehavior : Config.offlineMultiplierBehavior.getValue();
		case WITHDRAWAL_MULTIPLIER_BEHAVIOR:
			return Config.withdrawalMultiplierBehavior.getKey() ? withdrawalMultiplierBehavior : Config.withdrawalMultiplierBehavior.getValue();
		case ACCOUNT_CREATION_PRICE:
			return Config.creationPriceAccount.getKey() ? accountCreationPrice : Config.creationPriceAccount.getValue();
		case REIMBURSE_ACCOUNT_CREATION:
			return Config.reimburseAccountCreation.getKey() ? reimburseAccountCreation : Config.reimburseAccountCreation.getValue();
		case MINIMUM_BALANCE:
			return Config.minBalance.getKey() ? minBalance : Config.minBalance.getValue();
		case LOW_BALANCE_FEE:
			return Config.lowBalanceFee.getKey() ? lowBalanceFee : Config.lowBalanceFee.getValue();
		default:
			return null;
		}
	}
	
	public boolean setOrDefault(Field field, String s) throws NumberFormatException {
		
		if (!isOverrideAllowed(field))
			return false;
		
		switch (field) {
		
		case INTEREST_RATE:
			interestRate = Double.parseDouble(s.replace(",", ""));
			break;
		case MULTIPLIERS:
			multipliers = Arrays.stream(Utils.simplifyList(s).split(" ")).filter(string -> !string.equals(""))
					.map(string -> Integer.parseInt(string))
					.collect(Collectors.toList());
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
		case OFFLINE_MULTIPLAYER_BEHAVIOR:
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
		default:
			return false;
		}
		return true;
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

	public enum Field {

		INTEREST_RATE ("interest-rate", 0), 
		MULTIPLIERS ("multipliers", 3), 
		INITIAL_INTEREST_DELAY ("initial-interest-delay", 1), 
		COUNT_INTEREST_DELAY_OFFLINE ("count-interest-delay-offline", 2), 
		ALLOWED_OFFLINE_PAYOUTS ("allowed-offline-payouts", 1),
		ALLOWED_OFFLINE_PAYOUTS_BEFORE_MULTIPLIER_RESET ("allowed-offline-payouts-before-multiplier-reset", 1),
		OFFLINE_MULTIPLAYER_BEHAVIOR ("offline-multiplier-behavior", 1), 
		WITHDRAWAL_MULTIPLIER_BEHAVIOR ("withdrawal-multiplier-behavior", 1),
		ACCOUNT_CREATION_PRICE ("account-creation-price", 0), 
		REIMBURSE_ACCOUNT_CREATION ("reimburse-account-creation", 2), 
		MINIMUM_BALANCE ("min-balance", 0), 
		LOW_BALANCE_FEE ("low-balance-fee", 0);
		
		private String name;
		private int dataType;

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
