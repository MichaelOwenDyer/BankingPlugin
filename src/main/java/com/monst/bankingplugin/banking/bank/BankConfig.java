package com.monst.bankingplugin.banking.bank;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.banking.account.Account;
import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.exceptions.ArgumentParseException;
import com.monst.bankingplugin.utils.Callback;
import com.monst.bankingplugin.utils.Utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Defines the {@link Account} configuration for a specific {@link Bank}.
 * Bank owners are allowed to customize these configuration values in-game if the
 * corresponding "allow-override" value in the {@link Config} is marked as <b>true</b>.
 *
 * <p>This class statically initializes a {@link BiConsumer} ("SETTER") and a {@link Function} ("FORMATTER") for each {@link BankField}.
 * This is done either for all fields with a given type using {@link BankField#stream(Class[])} or, in some special cases,
 * individually.
 *
 * <p>The purpose of these static setters is to replace traditional, largely redundant setter methods which
 * must be individually created for each and every field. The formatters serve a similar function, preventing the
 * hassle of formatting the fetched value manually each and every time it is requested. Individual getters have also
 * been omitted from this class in favor of a single method {@link #get(BankField, boolean)} with a dynamic return type. All in all,
 * these generic functions help avoid repetitive boilerplate code and enable easier expansion, with few downsides.
 */
@SuppressWarnings("all")
public class BankConfig {

	private static final Map<BankField, BiConsumer<BankConfig, String>> SETTERS = new EnumMap<>(BankField.class);
	private static final Map<BankField, Function<BankConfig, String>> FORMATTERS = new EnumMap<>(BankField.class);
	static {
		/* Boolean */
		BankField.stream(Boolean.class).forEach(field -> { // Setters and formatters for fields of type Boolean
			SETTERS.put(field, (instance, value) -> {
				try {
					field.getLocalVariable().set(instance, Boolean.parseBoolean(value));
				} catch (IllegalAccessException ignored) {}
			});
			FORMATTERS.put(field, instance -> {
				try {
					return "" + field.getLocalVariable().get(instance);
				} catch (IllegalAccessException ignored) {}
				return "";
			});
		});

		/* Double */
		SETTERS.put(BankField.INTEREST_RATE, (instance, value) -> { // Special setter that accepts 3 decimal places
			try {
				BankField.INTEREST_RATE.getLocalVariable().set(instance, BigDecimal.valueOf(Double.parseDouble(Utils.removePunctuation(value, '.')))
						.abs().setScale(3, RoundingMode.HALF_UP).doubleValue());
			} catch (IllegalAccessException ignored) {}
		});
		FORMATTERS.put(BankField.INTEREST_RATE, instance -> { // Special formatter without $ symbol
			try {
				return new DecimalFormat("###,##0.00#").format(BankField.INTEREST_RATE.getLocalVariable().get(instance));
			} catch (IllegalAccessException ignored) {}
			return "";
		});
		BankField.stream(Double.class).forEach(field -> { // Setters and formatters for the rest of the fields of type Double
			SETTERS.putIfAbsent(field, (instance, value) -> {
				try {
					field.getLocalVariable().set(instance, BigDecimal.valueOf(Double.parseDouble(Utils.removePunctuation(value, '.')))
							.abs().setScale(2, RoundingMode.HALF_UP).doubleValue());
				} catch (IllegalAccessException ignored) {}
			});
			FORMATTERS.putIfAbsent(field, instance -> {
				try {
					return "$" + Utils.format((double) field.getLocalVariable().get(instance));
				} catch (IllegalAccessException ignored) {}
				return "";
			});
		});

		/* Integer */
		SETTERS.put(BankField.PLAYER_BANK_ACCOUNT_LIMIT, (instance, value) -> { // Special setter without Math.abs
			try {
				BankField.PLAYER_BANK_ACCOUNT_LIMIT.getLocalVariable().set(instance, Integer.parseInt(value));
			} catch (IllegalAccessException ignored) {}
		});
		BankField.stream(Integer.class).forEach(field -> { // Setters and formatters for the rest of the fields of type Integer
			SETTERS.putIfAbsent(field, (instance, value) -> {
				try {
					field.getLocalVariable().set(instance, Math.abs(Integer.parseInt(value)));
				} catch (IllegalAccessException ignored) {}
			});
			FORMATTERS.put(field, instance -> {
				try {
					return "" + field.getLocalVariable().get(instance);
				} catch (IllegalAccessException ignored) {}
				return "";
			});
		});

		/* List */
		SETTERS.put(BankField.MULTIPLIERS, (instance, value) -> { // Special setter that parses a List<Integer>
			try {
				BankField.MULTIPLIERS.getLocalVariable().set(instance,
						Arrays.stream(Utils.removePunctuation(value).split(" "))
						.filter(s -> !s.isEmpty())
						.map(Integer::parseInt)
						.map(Math::abs)
						.collect(Collectors.toList()));
			} catch (IllegalAccessException ignored) {}
		});
		SETTERS.put(BankField.INTEREST_PAYOUT_TIMES, (instance, value) -> { // Special setter that parses a List<LocalTime>
			try {
				BankField.INTEREST_PAYOUT_TIMES.getLocalVariable().set(instance,
						Arrays.stream(Utils.removePunctuation(value, ':').split(" "))
								.filter(s -> !s.isEmpty())
								.map(LocalTime::parse)
								.distinct()
								.sorted()
								.collect(Collectors.toList()));
			} catch (IllegalAccessException ignored) {}
		});
		BankField.stream(List.class).forEach(field -> {
			FORMATTERS.put(field, instance -> {
				try {
					return String.valueOf(field.getLocalVariable().get(instance));
				} catch (IllegalAccessException ignored) {}
				return "";
			});
		});
	}

	private boolean countInterestDelayOffline;
	private boolean reimburseAccountCreation;
	private boolean payOnLowBalance;
	private double interestRate;
	private double accountCreationPrice;
	private double minimumBalance;
	private double lowBalanceFee;
	private int initialInterestDelay;
	private int allowedOfflinePayouts;
	private int allowedOfflinePayoutsBeforeReset;
	private int offlineMultiplierDecrement;
	private int withdrawalMultiplierDecrement;
	private int playerBankAccountLimit;
	private List<Integer> multipliers;
	private List<LocalTime> interestPayoutTimes;

	/**
	 * Creates a new BankConfig with the default values from the {@link Config}.
	 */
	public static BankConfig mint() {
		return new BankConfig(
				Config.countInterestDelayOffline.getDefault(),
				Config.reimburseAccountCreation.getDefault(),
				Config.payOnLowBalance.getDefault(),
				Config.interestRate.getDefault(),
				Config.accountCreationPrice.getDefault(),
				Config.minimumBalance.getDefault(),
				Config.lowBalanceFee.getDefault(),
				Config.initialInterestDelay.getDefault(),
				Config.allowedOfflinePayouts.getDefault(),
				Config.allowedOfflinePayoutsBeforeReset.getDefault(),
				Config.offlineMultiplierDecrement.getDefault(),
				Config.withdrawalMultiplierDecrement.getDefault(),
				Config.playerBankAccountLimit.getDefault(),
				Config.multipliers.getDefault(),
				Config.interestPayoutTimes.getDefault()
		);
	}

	/**
	 * Re-creates an BankConfig that was stored in the {@link com.monst.bankingplugin.sql.Database}.
	 * @param countInterestDelayOffline whether the waiting period will decrease while account holders are offline
	 * @param reimburseAccountCreation whether account owners are reimbursed the (current) account creation price when removing an account
	 * @param payOnLowBalance whether interest will continue to be generated while an account balance is low
	 * @param interestRate the base interest rate, e.g. .01 or 1%
	 * @param accountCreationPrice the price to create an account at this bank
	 * @param minimumBalance the minimum balance for an account, below which the low balance fee must be paid
	 * @param lowBalanceFee the fee that will be charged to the account owner for each account he owns with a low balance
	 * @param initialInterestDelay the number of interest events until new accounts will generate interest for the first time
	 * @param allowedOfflinePayouts the number of consecutive times accounts may generate interest for offline holders
	 * @param allowedOfflinePayoutsBeforeReset the number of offline payouts before account multipliers reset
	 * @param offlineMultiplierDecrement how much account multipliers will decrease before every offline payout
	 * @param withdrawalMultiplierDecrement how much account multipliers will decrease on withdrawal
	 * @param playerBankAccountLimit the number of accounts each player is allowed to create at this bank
	 * @param multipliers a {@link List<Integer>} of multiplier values for the {@link Account}s at this {@link Bank}
	 * @param interestPayoutTimes a {@link List<LocalTime>} representing the times at which the bank will pay interest
	 */
	public BankConfig(boolean countInterestDelayOffline, boolean reimburseAccountCreation, boolean payOnLowBalance,
					  double interestRate, double accountCreationPrice, double minimumBalance, double lowBalanceFee,
					  int initialInterestDelay, int allowedOfflinePayouts, int allowedOfflinePayoutsBeforeReset,
					  int offlineMultiplierDecrement, int withdrawalMultiplierDecrement, int playerBankAccountLimit,
					  List<Integer> multipliers, List<LocalTime> interestPayoutTimes) {

		this.countInterestDelayOffline = countInterestDelayOffline;
		this.reimburseAccountCreation = reimburseAccountCreation;
		this.payOnLowBalance = payOnLowBalance;
		this.interestRate = interestRate;
		this.accountCreationPrice = accountCreationPrice;
		this.minimumBalance = minimumBalance;
		this.lowBalanceFee = lowBalanceFee;
		this.initialInterestDelay = initialInterestDelay;
		this.allowedOfflinePayouts = allowedOfflinePayouts;
		this.allowedOfflinePayoutsBeforeReset = allowedOfflinePayoutsBeforeReset;
		this.offlineMultiplierDecrement = offlineMultiplierDecrement;
		this.withdrawalMultiplierDecrement = withdrawalMultiplierDecrement;
		this.playerBankAccountLimit = playerBankAccountLimit;
		this.multipliers = multipliers;
		this.interestPayoutTimes = interestPayoutTimes;

	}

	/**
	 * Sets a value to the specified {@link BankField}. If the field cannot accept the
	 * provided value, a {@link ArgumentParseException} is returned in the {@link Callback}
	 * @param field the field to set
	 * @param value the value the field should be set to
	 * @param callback the {@link Callback} that returns the new formatted field or an error message
	 * @return whether the field is overridable or not
	 */
	boolean set(BankField field, String value, Callback<String> callback) {
		if (!field.isOverrideAllowed())
			return false;
		try {
			if (value.trim().isEmpty()) // Set to default
				field.getLocalVariable().set(this, field.getConfigPair().getDefault());
			else
				SETTERS.get(field).accept(this, value);
			callback.callSyncResult(getFormatted(field));
		} catch (NumberFormatException | DateTimeParseException e) {
			callback.callSyncError(new ArgumentParseException(field.getDataType(), value));
		} catch (IllegalAccessException e) {
			BankingPlugin.getInstance().debug(e);
		}
		return true;
	}

	String getFormatted(BankField field) {
		return FORMATTERS.get(field).apply(this);
	}

	/**
	 * Returns the current value of an {@link BankField}, or the default value from the {@link Config} if the field is
	 * not overridable and ignoreConfig is false.
	 * @param field the field to be looked up
	 * @param ignoreConfig whether to force returning the bank-specific value as opposed to potentially
	 *                     the default value from the {@link Config}
	 * @return the bank-specific value, or the default value if the field is currently not overridable
	 * @see BankField#isOverrideAllowed()
	 */
	@SuppressWarnings("unchecked")
	<T> T get(BankField field, boolean ignoreConfig) {
	    try {
	        if (ignoreConfig)
                return (T) field.getDataType().cast(field.getLocalVariable().get(this));
	        else
	            return field.isOverrideAllowed() ?
						(T) field.getDataType().cast(field.getLocalVariable().get(this)) :
						(T) field.getConfigPair().getDefault();
        } catch (IllegalAccessException e) {
            BankingPlugin.getInstance().debug(e);
            return null;
        }
    }
}
