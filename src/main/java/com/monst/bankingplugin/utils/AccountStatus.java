package com.monst.bankingplugin.utils;


import java.util.List;

/**
 * Defines the current payout status of an {@link com.monst.bankingplugin.Account} and provides
 * arithmetic for determining whether to pay out interest, among other things.
 */
public class AccountStatus {
	
	private final AccountConfig accountConfig;

	private int multiplierStage;
	private int delayUntilNextPayout;
	private int remainingOfflinePayouts;
	private int remainingOfflineUntilReset;

	/**
	 * Mint an AccountStatus for a brand new account.
	 */
	public static AccountStatus mint(AccountConfig config) {
		return new AccountStatus(
				config,
				0,
				config.get(AccountConfig.Field.INITIAL_INTEREST_DELAY),
				config.get(AccountConfig.Field.ALLOWED_OFFLINE_PAYOUTS),
				config.get(AccountConfig.Field.ALLOWED_OFFLINE_PAYOUTS_BEFORE_RESET)
		);
	}

	/**
	 * Creates an account status with the given values.
	 * @param config The AccountConfig from the account's bank
	 * @param multiplierStage The current multiplier stage of the account
	 * @param delayUntilNextPayout The initial delay value
	 * @param remainingOfflinePayouts How many offline payouts are currently remaining
	 * @param remainingOfflinePayoutsBeforeReset How many offline payouts are currently remaining until multiplier reset
	 */
	public AccountStatus(AccountConfig config, int multiplierStage, int delayUntilNextPayout,
			int remainingOfflinePayouts, int remainingOfflinePayoutsBeforeReset) {
		this.accountConfig = config;
		this.multiplierStage = multiplierStage;
		this.delayUntilNextPayout = delayUntilNextPayout;
		this.remainingOfflinePayouts = remainingOfflinePayouts;
		this.remainingOfflineUntilReset = remainingOfflinePayoutsBeforeReset;
	}
	
	public int getMultiplierStage() {
		return multiplierStage;
	}

	public int getDelayUntilNextPayout() {
		return delayUntilNextPayout;
	}

	public int getRemainingOfflinePayouts() {
		return remainingOfflinePayouts;
	}
	
	public int getRemainingOfflineUntilReset() {
		return remainingOfflineUntilReset;
	}
	
	public int processWithdrawal() {
		int increment = accountConfig.get(AccountConfig.Field.WITHDRAWAL_MULTIPLIER_DECREMENT);
		if (increment > 0)
			multiplierStage = Math.max(multiplierStage - increment, 0);
		else if (increment < 0)
			resetMultiplierStage();
		return multiplierStage;
	}

	/**
	 * Increments this account's multiplier stage.
	 * @param online whether the player is online or offline
	 */
	public void incrementMultiplier(boolean online) {

		List<Integer> multipliers = accountConfig.get(AccountConfig.Field.MULTIPLIERS);

		if (online) {
			if (multiplierStage < multipliers.size() - 1)
				multiplierStage++;
			remainingOfflineUntilReset = accountConfig.get(AccountConfig.Field.ALLOWED_OFFLINE_PAYOUTS_BEFORE_RESET);
		} else {
			if (remainingOfflineUntilReset == 0) {
				resetMultiplierStage();
				return;
			} else if (remainingOfflineUntilReset > 0)
				remainingOfflineUntilReset--;

			int increment = accountConfig.get(AccountConfig.Field.OFFLINE_MULTIPLIER_DECREMENT);
			int newStage = multiplierStage + increment;
			
			if (newStage < 0) {
				multiplierStage = 0;
			} else if (newStage >= multipliers.size()) {
				multiplierStage = multipliers.size() - 1;
			} else
				multiplierStage = newStage;
		}
	}
	
	/**
	 * Determines whether to allow the next interest payout or not.
	 * 
	 * @param online whether the player is online or not
	 * @return True if interest payout is allowed, or false if not.
	 */
	public boolean allowNextPayout(boolean online) {
		if (online) {
			if (delayUntilNextPayout > 0) {
				delayUntilNextPayout--;
				return false;
			}

			return true;
		} else {
			if (delayUntilNextPayout > 0) {
				if (accountConfig.get(AccountConfig.Field.COUNT_INTEREST_DELAY_OFFLINE))
					delayUntilNextPayout--;
				return false;
			} else
				if (remainingOfflinePayouts > 0) {
					remainingOfflinePayouts--;
					return true;
				}
			return false;
		}
	}
	
	/**
	 * Gets the multiplier from Config:interestMultipliers corresponding to this account's current multiplier stage.
	 * @return the corresponding multiplier, or 1x by default in case of an error.
	 */
	public int getRealMultiplier() {
		
		List<Integer> multipliers = accountConfig.get(AccountConfig.Field.MULTIPLIERS);

		if (multiplierStage < 0)
			multiplierStage = 0;
		else if (multiplierStage >= multipliers.size())
			multiplierStage = multipliers.size() - 1;
		return multipliers.isEmpty() ? 1 : multipliers.get(multiplierStage);
	}
	
	/**
	 * Resets this account's multiplier stage to zero, the first index of the config list.
	 */
	public void resetMultiplierStage() {
		multiplierStage = 0;
	}
	
	public int setMultiplierStage(int stage) {

		List<Integer> multipliers = accountConfig.get(AccountConfig.Field.MULTIPLIERS);

		stage--;
		if (stage < 0)
			multiplierStage = 0;
		else if (stage >= multipliers.size())
			multiplierStage = multipliers.size() - 1;
		else
			multiplierStage = stage;
		return multiplierStage;
	}

	public int setMultiplierStageRelative(int stage) {
		return setMultiplierStage(multiplierStage + ++stage);
	}

	public int setInterestDelay(int delay) {
		return delayUntilNextPayout = Math.max(delay, 0);
	}

	public int setInterestDelayRelative(int delay) {
		return setInterestDelay(delayUntilNextPayout + delay);
	}
}