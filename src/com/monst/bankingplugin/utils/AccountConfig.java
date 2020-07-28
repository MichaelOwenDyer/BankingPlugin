package com.monst.bankingplugin.utils;

import com.monst.bankingplugin.Account;
import com.monst.bankingplugin.Bank;
import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.exceptions.ArgumentParseException;
import org.apache.commons.lang.WordUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Defines the {@link Account} configuration for a specific {@link Bank}.
 * Bank owners are allowed to customize these configuration values in-game if the
 * corresponding "allow-override" value in the {@link Config} is marked as <b>true</b>.
 */
@SuppressWarnings("all")
public class AccountConfig {

	private final Map<Field, Function<String, String>> SETTERS = new EnumMap<>(Field.class);

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

		initialize();
	}

	/**
	 * Reports whether or not a {@link Field} is set as "allow-override: true" in the {@link Config}.
	 * @param field the configuration value
	 * @return whether a config value can be set independently for each bank
	 */
	@SuppressWarnings("rawtypes")
	public static boolean isOverrideAllowed(Field field) {
		try {
			return (boolean) ((AbstractMap.SimpleEntry) field.getConfigField().get(null)).getKey();
		} catch (IllegalAccessException e) {
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
			callback.callSyncResult(SETTERS.get(field).apply(value));
		} catch (NumberFormatException e) {
			callback.callSyncError(new ArgumentParseException(field.getDataType(), value));
			return false;
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
	@SuppressWarnings({"unchecked", "rawtypes"})
	public <T> T get(Field field, boolean ignoreConfig) {
	    try {
	        if (ignoreConfig)
                return (T) field.getDataType().cast(field.getLocalField().get(this));
	        else
	            return isOverrideAllowed(field)
                            ? (T) field.getDataType().cast(field.getLocalField().get(this))
                            : (T) field.getDataType().cast(((AbstractMap.SimpleEntry) field.getConfigField().get(null)).getValue());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

	private void initialize() {

		Field.stream(Boolean.class).forEach(field -> SETTERS.put(field, (value) -> {
			try {
				field.getLocalField().set(this, Boolean.parseBoolean(value));
				return "" + field.getLocalField().get(this);
			} catch (IllegalAccessException ignored) {}
			return "";
		}));

		Field.stream(Double.class).forEach(field -> SETTERS.put(field, (value) -> {
			try {
				field.getLocalField().set(this, Math.abs(Double.parseDouble(value.replace(",", ""))));
				return Utils.format((double) field.getLocalField().get(this));
			} catch (IllegalAccessException ignored) {}
			return "";
		}));

		SETTERS.put(Field.PLAYER_BANK_ACCOUNT_LIMIT, (value) -> {
			playerBankAccountLimit = Integer.parseInt(value); // Special setter without Math.abs for this field
			return "" + playerBankAccountLimit;
		});
		Field.stream(Integer.class).forEach(field -> SETTERS.putIfAbsent(field, (value) -> {
			try {
				field.getLocalField().set(this, Math.abs(Integer.parseInt(value)));
				return "" + field.getLocalField().get(this);
			} catch (IllegalAccessException e) {}
			return "";
		}));

		Field.stream(List.class).forEach(field -> SETTERS.put(field, (value) -> {
			try {
				field.getLocalField().set(this, Arrays.stream(Utils.removePunctuation(value)
						.split(" ")).filter(t -> !t.isEmpty())
						.map(Integer::parseInt).map(Math::abs).collect(Collectors.toList()));
				return Utils.formatList((List<? extends Number>) field.getLocalField().get(this));
			} catch (IllegalAccessException e) {}
			return "";
		}));
	}

	/**
	 * Represents all the bank configuration values for a given bank.
	 */
	public enum Field {

		INTEREST_RATE (Double.class),
		MULTIPLIERS (List.class),
		INITIAL_INTEREST_DELAY (Integer.class),
		COUNT_INTEREST_DELAY_OFFLINE (Boolean.class),
		ALLOWED_OFFLINE_PAYOUTS (Integer.class),
		ALLOWED_OFFLINE_PAYOUTS_BEFORE_RESET (Integer.class),
		OFFLINE_MULTIPLIER_DECREMENT (Integer.class),
		WITHDRAWAL_MULTIPLIER_DECREMENT (Integer.class),
		ACCOUNT_CREATION_PRICE (Double.class),
		REIMBURSE_ACCOUNT_CREATION (Boolean.class),
		MINIMUM_BALANCE (Double.class),
		LOW_BALANCE_FEE (Double.class),
		PAY_ON_LOW_BALANCE (Boolean.class),
		PLAYER_BANK_ACCOUNT_LIMIT (Integer.class);

		private final String name;
		private final Class<?> dataType;
		private java.lang.reflect.Field localField;
		private java.lang.reflect.Field configField;

		Field(Class<?> dataType) {
			this.name = toString().toLowerCase().replace("_", "-");
			this.dataType = dataType;
            try {
                String fieldName = WordUtils.capitalize(toString().replace("_", " ")).replace(" ", "");
                this.localField = AccountConfig.class.getDeclaredField(fieldName);
                this.configField = Config.class.getField(fieldName);
            } catch (NoSuchFieldException ignored) {}
		}

		/**
		 * @return the name of this field
		 */
		public String getName() {
			return name;
		}

		/**
		 * @return the type of this field, e.g. Double, Integer, Boolean, or List
		 */
		public Class<?> getDataType() {
			return dataType;
		}

		/**
		 * @return the bank-specific value of this field, stored in {@link AccountConfig}
		 */
		private java.lang.reflect.Field getLocalField() {
		    return localField;
        }

		/**
		 * @return the default value of this field, stored in {@link Config}
		 */
		private java.lang.reflect.Field getConfigField() {
		    return configField;
        }

		/**
		 * @return a {@link Stream<Field>} containing all fields
		 */
		public static Stream<Field> stream() {
			return Stream.of(Field.values());
		}

		/**
		 * @param types the types to match
		 * @return a {@link Stream<Field>} containing all fields that match one of the specified data types
		 * @see #getDataType()
		 */
		public static Stream<Field> stream(Class<?>... types) {
			return stream().filter(f -> Arrays.asList(types).contains(f.getDataType()));
		}

		/**
		 * @param name the name of the field
		 * @return the field with the specified name
		 */
		public static Field getByName(String name) {
			return stream().filter(bankField -> bankField.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
		}
	}
}
