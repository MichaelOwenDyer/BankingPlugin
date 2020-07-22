package com.monst.bankingplugin.utils;

import java.util.List;

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
		this(config, 0, config.getInitialInterestDelay(false),
				config.getAllowedOfflinePayouts(false),
				config.getAllowedOfflineBeforeReset(false));
	}
	/**
	 * Creates an account status with the given values.
	 * @param config The AccountConfig from the account's bank
	 * @param multiplierStage The current multiplier stage of the account
	 * @param delayUntilNextPayout The initial delay value
	 * @param remainingOfflinePayouts How many offline payouts are currently remaining
	 * @param remainingOfflineUntilReset How many offline payouts are currently remaining until multiplier reset
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
		int increment = accountConfig.getWithdrawalMultiplierBehavior(false);

		if (increment > 0) {
			resetMultiplierStage();
			return 0;
		} else if (increment == 0)
			return multiplierStage;

		multiplierStage = Math.max(multiplierStage + increment, 0);

		return multiplierStage;
	}

	/**
	 * Increments this account's multiplier stage.
	 * @param online whether the player is online or offline
	 */
	public void incrementMultiplier(boolean online) {

		List<Integer> multipliers = accountConfig.getMultipliers(false);

		if (online) {
			if (multiplierStage < multipliers.size() - 1)
				multiplierStage++;
			remainingOfflineUntilReset = accountConfig.getAllowedOfflineBeforeReset(false);
		} else {
			if (remainingOfflineUntilReset == 0) {
				resetMultiplierStage();
				return;
			} else if (remainingOfflineUntilReset > 0)
				remainingOfflineUntilReset--;

			int increment = accountConfig.getOfflineMultiplierBehavior(false);
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
				if (accountConfig.isCountInterestDelayOffline(false))
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
		
		List<Integer> multipliers = accountConfig.getMultipliers(false);

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

		List<Integer> multipliers = accountConfig.getMultipliers(false);

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

		List<Integer> multipliers = accountConfig.getMultipliers(false);

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
		delayUntilNextPayout = Math.max(delay, 0);
		return delayUntilNextPayout;
	}
}
