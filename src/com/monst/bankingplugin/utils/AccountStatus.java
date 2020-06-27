package com.monst.bankingplugin.utils;

import java.util.List;

public class AccountStatus {
	
	private final AccountConfig accountConfig;

	private int multiplierStage;
	private int remainingUntilPayout;
	private int remainingOfflinePayouts;
	private int remainingOfflineUntilReset;
		
	/**
	 * <p>Default AccountStatus constructor for a brand new account.</p>
	 */
	public AccountStatus(AccountConfig config) {
		this(config, 0, config.getInitialInterestDelayOrDefault(), config.getAllowedOfflinePayoutsOrDefault(),
				config.getAllowedOfflineBeforeResetOrDefault());
	}
	/**
	 * Creates an account status with the given values.
	 * @param multiplierStage
	 * @param remainingOfflinePayouts
	 * @param remainingUntilFirstPayout
	 * @param balance
	 * @param prevBalance
	 */
	public AccountStatus(AccountConfig config, int multiplierStage, int remainingUntilFirstPayout,
			int remainingOfflinePayouts, int remainingOfflineUntilReset) {
		this.accountConfig = config;
		this.multiplierStage = multiplierStage;
		this.remainingUntilPayout = remainingUntilFirstPayout;
		this.remainingOfflinePayouts = remainingOfflinePayouts;
		this.remainingOfflineUntilReset = remainingOfflineUntilReset;
	}
	
	public int getMultiplierStage() {
		return multiplierStage;
	}

	public int getRemainingUntilFirstPayout() {
		return remainingUntilPayout;
	}

	public int getRemainingOfflinePayouts() {
		return remainingOfflinePayouts;
	}
	
	public int getRemainingOfflineUntilReset() {
		return remainingOfflineUntilReset;
	}
	
	public int processWithdrawal() {
		int increment = accountConfig.getWithdrawalMultiplierBehaviorOrDefault();

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
		if (online) {
			if (multiplierStage < accountConfig.getMultipliersOrDefault().size() - 1)
				multiplierStage++;
			
		} else {
			if (remainingOfflineUntilReset > 0)
				remainingOfflineUntilReset--;
			else {
				resetMultiplierStage();
				return 0;
			}
			int increment = accountConfig.getOfflineMultiplierBehaviorOrDefault();
			int newStage = multiplierStage + increment;
			
			if (newStage < 0) {
				multiplierStage = 0;
			} else if (newStage >= accountConfig.getMultipliersOrDefault().size()) {
				multiplierStage = accountConfig.getMultipliersOrDefault().size() - 1;
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
			if (remainingUntilPayout > 0) {
				remainingUntilPayout--;
				return false;
			}
			remainingOfflineUntilReset = accountConfig.getAllowedOfflineBeforeResetOrDefault();
			return true;
		} else {
			if (remainingUntilPayout > 0) {
				if (accountConfig.isCountInterestDelayOfflineOrDefault())
					remainingUntilPayout--;
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
		
		if (multiplierStage < 0)
			multiplierStage = 0;
		else if (multiplierStage >= accountConfig.getMultipliersOrDefault().size())
			multiplierStage = accountConfig.getMultipliersOrDefault().size() - 1;
		List<Integer> multipliers = accountConfig.getMultipliersOrDefault();
		return multipliers.isEmpty() ? 1 : multipliers.get(multiplierStage);
	}
	
	/**
	 * Resets this account's multiplier stage to zero, the first index of the config list.
	 */
	public void resetMultiplierStage() {
		multiplierStage = 0;
	}
	
	public int setMultiplierStage(int stage) {
		stage--;
		if (stage < 0)
			multiplierStage = 0;
		else if (stage >= accountConfig.getMultipliersOrDefault().size())
			multiplierStage = accountConfig.getMultipliersOrDefault().size() - 1;
		else
			multiplierStage = stage;
		return multiplierStage;
	}

	public int setMultiplierStageRelative(int stage) {
		int newStage = multiplierStage + stage;
		if (newStage < 0)
			multiplierStage = 0;
		else if (newStage >= accountConfig.getMultipliersOrDefault().size())
			multiplierStage = accountConfig.getMultipliersOrDefault().size() - 1;
		else
			multiplierStage = newStage;
		return multiplierStage;
	}

	public int setInterestDelay(int delay) {
		if (delay <= 0)
			remainingUntilPayout = 0;
		else
			remainingUntilPayout = delay;
		return remainingUntilPayout;
	}
}
