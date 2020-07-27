package com.monst.bankingplugin.utils;

import com.monst.bankingplugin.Account;
import com.monst.bankingplugin.Bank;
import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.config.Config;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Defines the {@link Account} configuration for a specific {@link Bank}.
 * Bank owners are allowed to customize these configuration values in-game if the
 * corresponding "allow-override" value in the {@link Config} is marked as <b>true</b>.
 */
@SuppressWarnings("unused")
public class AccountConfig {

	private static boolean initialized = false;
	private static final Map<Field, Method> SETTERS = new EnumMap<>(Field.class);
	private static final Map<Field, java.lang.reflect.Field> CONFIG_FIELDS = new EnumMap<>(Field.class);
	private static final Map<Field, java.lang.reflect.Field> LOCAL_FIELDS = new EnumMap<>(Field.class);

	public static void initialize() {
		if (initialized)
			return;
		for (Field field : Field.values()) {
			try {
				SETTERS.put(field, AccountConfig.class.getDeclaredMethod(field.getSetterName(), String.class));
				LOCAL_FIELDS.put(field, AccountConfig.class.getDeclaredField(field.getFieldName()));
				CONFIG_FIELDS.put(field, Config.class.getField(field.getFieldName()));
			} catch (NoSuchMethodException e) {
				BankingPlugin.getInstance().debug("BankField method error: could not find method for \"" + field.getSetterName() + "\"");
			} catch (NoSuchFieldException e) {
				BankingPlugin.getInstance().debug("BankField field error: could not find field for \"" + field.getFieldName() + "\"");
			}
		}
		initialized = true;
	}

	private double interestRate;
	private List<Integer> multipliers;
	private int initialInterestDelay;
	private boolean countInterestDelayOffline;
	private int allowedOfflinePayouts;
	private int allowedOfflinePayoutsBeforeReset;
	private int offlineMultiplierDecrement;
	private int withdrawalMultiplierDecrement;
	private double accountCreationPrice;
	private boolean reimburseAccountCreation;
	private double minBalance;
	private double lowBalanceFee;
	private boolean payOnLowBalance;
	private int playerBankAccountLimit;

	/**
	 * Create a new AccountConfig with the default values from the {@link Config}.
	 */
	public AccountConfig() {
		this(
				Config.interestRate.getValue(),
				Config.multipliers.getValue(),
				Config.initialInterestDelay.getValue(),
				Config.countInterestDelayOffline.getValue(),
				Config.allowedOfflinePayouts.getValue(),
				Config.allowedOfflinePayoutsBeforeReset.getValue(),
				Config.offlineMultiplierDecrement.getValue(),
				Config.withdrawalMultiplierDecrement.getValue(),
				Config.accountCreationPrice.getValue(),
				Config.reimburseAccountCreation.getValue(),
				Config.minBalance.getValue(),
				Config.lowBalanceFee.getValue(),
				Config.payOnLowBalance.getValue(),
				Config.playerBankAccountLimit.getValue()
			);
	}

	/**
	 * Re-create an AccountConfig that was stored in the {@link com.monst.bankingplugin.sql.Database}.
	 * @param interestRate the base interest rate, e.g. .01 or 1%
	 * @param multipliers a {@link List<Integer>} of multiplier values for the {@link Account}s at this {@link Bank}
	 * @param initialInterestDelay the number of interest events until an account will generate interest for the first time
	 * @param countInterestDelayOffline whether the waiting period will decrease while the account owner is offline
	 * @param allowedOfflinePayouts the number of consecutive times an account may generate interest for an offline owner
	 * @param allowedOfflinePayoutsBeforeReset the number of offline payouts before an account multiplier resets
	 * @param offlineMultiplierDecrement how much an account multiplier will decrease before every offline payout
	 * @param withdrawalMultiplierDecrement how much an account multiplier will decrease on withdrawal
	 * @param accountCreationPrice the price to create an account at this bank
	 * @param reimburseAccountCreation whether account owners are reimbursed the (current) account creation price when removing an account
	 * @param minBalance the minimum balance for an account
	 * @param lowBalanceFee the fee that will be charged to the account owner for each account he owns with a low balance
	 * @param payOnLowBalance whether interest will continue to be generated while an account balance is low
	 * @param playerBankAccountLimit the number of accounts each player is allowed to create at this bank
	 */
	public AccountConfig(double interestRate, List<Integer> multipliers, int initialInterestDelay,
						 boolean countInterestDelayOffline, int allowedOfflinePayouts, int allowedOfflinePayoutsBeforeReset,
						 int offlineMultiplierDecrement, int withdrawalMultiplierDecrement, double accountCreationPrice,
						 boolean reimburseAccountCreation, double minBalance, double lowBalanceFee, boolean payOnLowBalance, int playerBankAccountLimit) {

		this.interestRate = interestRate;
		this.multipliers = multipliers;
		this.initialInterestDelay = initialInterestDelay;
		this.countInterestDelayOffline = countInterestDelayOffline;
		this.allowedOfflinePayouts = allowedOfflinePayouts;
		this.allowedOfflinePayoutsBeforeReset = allowedOfflinePayoutsBeforeReset;
		this.offlineMultiplierDecrement = offlineMultiplierDecrement;
		this.withdrawalMultiplierDecrement = withdrawalMultiplierDecrement;
		this.accountCreationPrice = accountCreationPrice;
		this.reimburseAccountCreation = reimburseAccountCreation;
		this.minBalance = minBalance;
		this.lowBalanceFee = lowBalanceFee;
		this.payOnLowBalance = payOnLowBalance;
		this.playerBankAccountLimit = playerBankAccountLimit;

	}

	/**
	 * Reports whether or not a {@link Field} is set as "allow-override: true" in the {@link Config}.
	 * @param field the configuration value
	 * @return whether a config value can be set independently for each bank
	 */
	@SuppressWarnings("rawtypes")
	public static boolean isOverrideAllowed(Field field) {
		try {
			return (boolean) ((AbstractMap.SimpleEntry) CONFIG_FIELDS.get(field).get(null)).getKey();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Set a value to the specified {@link Field}. If the field cannot accept the
	 * provided value, a {@link NumberFormatException} is returned in the {@link Callback}
	 * @param field the field to set
	 * @param value the value to set the field to
	 * @param callback the {@link Callback} that returns how the value was parsed and interpreted
	 * @return whether the field was successfully set or not
	 */
	public boolean set(Field field, String value, Callback<String> callback) {
		
		if (!isOverrideAllowed(field))
			return false;

		try {
			callback.callSyncResult((String) SETTERS.get(field).invoke(this, value));
		} catch (NumberFormatException e) {
			callback.callSyncError(e);
			return false;
		} catch (IllegalAccessException | InvocationTargetException e) {
			e.printStackTrace();
		}
		return true;
	}

	public <T> T get(Field field) {
		return get(field, false);
	}

	/**
	 * A handy lookup method that takes a {@link Field} and returns its current value.
	 * @param field the field to be looked up
	 * @param ignoreConfig whether to force returning the bank-specific value as opposed to potentially
	 *                     the default value from the {@link Config}
	 * @return the bank-specific value, or the default value if the field is currently not overridable
	 * @see #isOverrideAllowed(Field)
	 */
	@SuppressWarnings("unchecked")
	public <T> T get(Field field, boolean ignoreConfig) {
		switch (field.getDataType()) {
			case 0:
				return (T) get(field, ignoreConfig, 0.0d);
			case 1:
				return (T) get(field, ignoreConfig, 0);
			case 2:
				return (T) get(field, ignoreConfig, true);
			case 3:
				return (T) get(field, ignoreConfig, new ArrayList<Integer>());
		}
		return null;
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private <T> T get(Field field, boolean ignoreConfig, T type) {
		try {
			if (ignoreConfig)
				return (T) LOCAL_FIELDS.get(field).get(this);
			else {
				return isOverrideAllowed(field)
						? (T) LOCAL_FIELDS.get(field).get(this)
						: (T) ((AbstractMap.SimpleEntry) CONFIG_FIELDS.get(field).get(null)).getValue();
			}
		} catch (IllegalAccessException ignored) {}
		return null;
	}

	private String setInterestRate(String value) throws NumberFormatException {
		interestRate = Double.parseDouble(value.replace(",", ""));
		return Utils.formatNumber(interestRate);
	}

	private String setMultipliers(String value) throws NumberFormatException {
		multipliers = Arrays.stream(Utils.removePunctuation(value)
				.split(" ")).filter(t -> !t.isEmpty())
				.map(Integer::parseInt).collect(Collectors.toList());
		return Utils.formatList(multipliers);
	}

	private String setInitialInterestDelay(String value) throws NumberFormatException {
		initialInterestDelay = Math.abs(Integer.parseInt(value));
		return "" + initialInterestDelay;
	}

	private String setCountInterestDelayOffline(String value) throws NumberFormatException {
		if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false"))
			countInterestDelayOffline = Boolean.parseBoolean(value);
		else
			throw new NumberFormatException();
		return "" + countInterestDelayOffline;
	}

	private String setAllowedOfflinePayouts(String value) throws NumberFormatException {
		allowedOfflinePayouts = Math.abs(Integer.parseInt(value));
		return "" + allowedOfflinePayouts;
	}

	private String setAllowedOfflinePayoutsBeforeReset(String value) throws NumberFormatException {
		allowedOfflinePayoutsBeforeReset = Math.abs(Integer.parseInt(value));
		return "" + allowedOfflinePayoutsBeforeReset;
	}

	private String setOfflineMultiplierDecrement(String value) throws NumberFormatException {
		offlineMultiplierDecrement = Math.abs(Integer.parseInt(value));
		return "" + offlineMultiplierDecrement;
	}

	private String setWithdrawalMultiplierDecrement(String value) throws NumberFormatException {
		withdrawalMultiplierDecrement = Math.abs(Integer.parseInt(value));
		return "" + withdrawalMultiplierDecrement;
	}

	private String setAccountCreationPrice(String value) throws NumberFormatException {
		accountCreationPrice = Math.abs(Double.parseDouble(value.replace(",", "")));
		return Utils.formatNumber(accountCreationPrice);
	}

	private String setReimburseAccountCreation(String value) throws NumberFormatException {
		if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false"))
			reimburseAccountCreation = Boolean.parseBoolean(value);
		else
			throw new NumberFormatException();
		return "" + reimburseAccountCreation;
	}

	private String setMinBalance(String value) throws NumberFormatException {
		minBalance = Math.abs(Double.parseDouble(value.replace(",", "")));
		return Utils.formatNumber(minBalance);
	}

	private String setLowBalanceFee(String value) throws NumberFormatException {
		lowBalanceFee = Math.abs(Double.parseDouble(value.replace(",", "")));
		return Utils.formatNumber(lowBalanceFee);
	}

	private String setPayOnLowBalance(String value) throws NumberFormatException {
		if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false"))
			payOnLowBalance = Boolean.parseBoolean(value);
		else
			throw new NumberFormatException();
		return "" + payOnLowBalance;
	}

	private String setPlayerBankAccountLimit(String value) throws NumberFormatException {
		playerBankAccountLimit = Math.abs(Integer.parseInt(value));
		return "" + playerBankAccountLimit;
	}

	@Override
	public String toString() {
		return Arrays.stream(Field.values()).map(field -> field.getName() + ": " + isOverrideAllowed(field)).collect(Collectors.joining("\n"));
	}

	/**
	 * Represents all the bank configuration values for a given bank.
	 */
	public enum Field {

		INTEREST_RATE ("interest-rate", "InterestRate", 0),
		MULTIPLIERS ("multipliers", "Multipliers", 3),
		INITIAL_INTEREST_DELAY ("initial-interest-delay", "InitialInterestDelay", 1),
		COUNT_INTEREST_DELAY_OFFLINE ("count-interest-delay-offline", "CountInterestDelayOffline", 2),
		ALLOWED_OFFLINE_PAYOUTS ("allowed-offline-payouts", "AllowedOfflinePayouts", 1),
		ALLOWED_OFFLINE_PAYOUTS_BEFORE_RESET("allowed-offline-payouts-before-reset", "AllowedOfflinePayoutsBeforeReset", 1),
		OFFLINE_MULTIPLIER_DECREMENT ("offline-multiplier-decrement", "OfflineMultiplierDecrement", 1),
		WITHDRAWAL_MULTIPLIER_DECREMENT ("withdrawal-multiplier-decrement", "WithdrawalMultiplierDecrement", 1),
		ACCOUNT_CREATION_PRICE ("account-creation-price", "AccountCreationPrice", 0),
		REIMBURSE_ACCOUNT_CREATION ("reimburse-account-creation", "ReimburseAccountCreation", 2),
		MINIMUM_BALANCE ("minimum-balance", "MinBalance", 0),
		LOW_BALANCE_FEE ("low-balance-fee", "LowBalanceFee", 0),
		PAY_ON_LOW_BALANCE ("pay-on-low-balance", "PayOnLowBalance", 2),
		PLAYER_BANK_ACCOUNT_LIMIT("player-account-limit", "PlayerBankAccountLimit", 1);
		
		private final String name;
		private final String methodName;
		private final int dataType; // double: 0, integer: 1, boolean: 2, list: 3

		Field(String name, String methodName, int dataType) {
			this.name = name;
			this.methodName = methodName;
			this.dataType = dataType;
		}

		public String getName() {
			return name;
		}

		private String getGetterName() {
			return "get" + methodName;
		}

		private String getSetterName() {
			return "set" + methodName;
		}

		private String getFieldName() {
			return methodName.substring(0, 1).toLowerCase() + methodName.substring(1);
		}

		public int getDataType() {
			return dataType;
		}

		public static Stream<Field> stream() {
			return Stream.of(Field.values());
		}

		public static Field getByName(String name) {
			return stream().filter(bankField -> bankField.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
		}
	}
}
