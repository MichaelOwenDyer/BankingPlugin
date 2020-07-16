package com.monst.bankingplugin.utils;

import java.util.List;

import com.monst.bankingplugin.utils.AccountConfig.Field;

public class AccountStatus {
	
	private final AccountConfig accountConfig;

	private int multiplierStage;
	private int delayUntilNextPayout;
	private int remainingOfflinePayouts;
	private int remainingOfflineUntilReset;
		
	/**
	 * <p>Default AccountStatus constructor for a brand new account.</p>
	 */
	public AccountStatus(AccountConfig config) {
		this(config, 0, (int) config.getOrDefault(Field.INITIAL_INTEREST_DELAY),
				(int) config.getOrDefault(Field.ALLOWED_OFFLINE_PAYOUTS),
				(int) config.getOrDefault(Field.ALLOWED_OFFLINE_PAYOUTS_BEFORE_MULTIPLIER_RESET));
	}
	/**
	 * Creates an account status with the given values.
	 * @param multiplierStage
	 * @param remainingOfflinePayouts
	 * @param delayUntilNextPayout
	 * @param balance
	 * @param prevBalance
	 */
	public AccountStatus(AccountConfig config, int multiplierStage, int delayUntilNextPayout,
			int remainingOfflinePayouts, int remainingOfflineUntilReset) {
		this.accountConfig = config;
		this.multiplierStage = multiplierStage;
		this.delayUntilNextPayout = delayUntilNextPayout;
		this.remainingOfflinePayouts = remainingOfflinePayouts;
		this.remainingOfflineUntilReset = remainingOfflineUntilReset;
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
		int increment = (int) accountConfig.getOrDefault(Field.WITHDRAWAL_MULTIPLIER_BEHAVIOR);

		if (increment > 0) {
			resetMultiplierStage();
			return 0;
		} else if (increment == 0)
			return multiplierStage;

		int newStage = multiplierStage + increment;
		if (newStage < 0)
			multiplierStage = 0;
		else
			multiplierStage = newStage;

		return multiplierStage;
	}

	/**
	 * Increments this account's multiplier stage.
	 * @param boolean whether the player is online or offline
	 * @return the (possibly unchanged) multiplier stage of this account.
	 */
	public int incrementMultiplier(boolean online) {

		@SuppressWarnings("unchecked")
		List<Integer> multipliers = (List<Integer>) accountConfig.getOrDefault(Field.MULTIPLIERS);

		if (online) {
			if (multiplierStage < multipliers.size() - 1)
				multiplierStage++;
			
		} else {
			if (remainingOfflineUntilReset > 0)
				remainingOfflineUntilReset--;
			else {
				resetMultiplierStage();
				return 0;
			}
			int increment = (int) accountConfig.getOrDefault(Field.OFFLINE_MULTIPLIER_BEHAVIOR);
			int newStage = multiplierStage + increment;
			
			if (newStage < 0) {
				multiplierStage = 0;
			} else if (newStage >= multipliers.size()) {
				multiplierStage = multipliers.size() - 1;
			} else
				multiplierStage = newStage;
		}
		return multiplierStage;
	}
	
	/**
	 * Determines whether to allow the next interest payout or not.
	 * 
	 * @param boolean whether the player is online or not
	 * @return True if interest payout is allowed, or false if not.
	 */
	public boolean allowNextPayout(boolean online) {
		if (online) {
			if (delayUntilNextPayout > 0) {
				delayUntilNextPayout--;
				return false;
			}
			remainingOfflineUntilReset = (int) accountConfig.getOrDefault(Field.ALLOWED_OFFLINE_PAYOUTS_BEFORE_MULTIPLIER_RESET);
			return true;
		} else {
			if (delayUntilNextPayout > 0) {
				if ((boolean) accountConfig.getOrDefault(Field.COUNT_INTEREST_DELAY_OFFLINE))
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
		
		@SuppressWarnings("unchecked")
		List<Integer> multipliers = (List<Integer>) accountConfig.getOrDefault(Field.MULTIPLIERS);

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

		@SuppressWarnings("unchecked")
		List<Integer> multipliers = (List<Integer>) accountConfig.getOrDefault(Field.MULTIPLIERS);

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

		@SuppressWarnings("unchecked")
		List<Integer> multipliers = (List<Integer>) accountConfig.getOrDefault(Field.MULTIPLIERS);

		int newStage = multiplierStage + stage;
		if (newStage < 0)
			multiplierStage = 0;
		else if (newStage >= multipliers.size())
			multiplierStage = multipliers.size() - 1;
		else
			multiplierStage = newStage;
		return multiplierStage;
	}

	public int setInterestDelay(int delay) {
		if (delay <= 0)
			delayUntilNextPayout = 0;
		else
			delayUntilNextPayout = delay;
		return delayUntilNextPayout;
	}
}
