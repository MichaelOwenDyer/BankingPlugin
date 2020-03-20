package com.monst.bankingplugin.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.monst.bankingplugin.config.Config;

public class AccountStatus {
	
	private int multiplierStage;
	private int remainingUntilPayout;
	private int remainingOfflinePayouts;
	private int remainingOfflineUntilReset;
	private BigDecimal balance;
	private BigDecimal prevBalance;
		
	/**
	 * <p>
	 * Default AccountStatus constructor for a brand new account.
	 * </p>
	 * Equivalent to <b>new AccountStatus(0, Config.interestDelayPeriod,
	 * Config.allowedOfflinePayouts, Config.allowedOfflinePayoutsBeforeMultiplierReset, 0, 0)</b>
	 */
	public AccountStatus() {
		this(0, Config.interestDelayPeriod, Config.allowedOfflinePayouts, Config.allowedOfflineBeforeMultiplierReset,
				BigDecimal.ZERO, BigDecimal.ZERO);
	}
	/**
	 * Creates an account status with the given values.
	 * @param multiplierStage
	 * @param remainingOfflinePayouts
	 * @param remainingUntilFirstPayout
	 * @param balance
	 * @param prevBalance
	 */
	public AccountStatus(int multiplierStage, int remainingUntilFirstPayout, int remainingOfflinePayouts,
			int remainingOfflineUntilReset, BigDecimal balance, BigDecimal prevBalance) {
		this.multiplierStage = multiplierStage;
		this.remainingUntilPayout = remainingUntilFirstPayout;
		this.remainingOfflinePayouts = remainingOfflinePayouts;
		this.remainingOfflineUntilReset = remainingOfflineUntilReset;
		this.balance = balance.setScale(2, RoundingMode.HALF_EVEN);
		this.prevBalance = prevBalance.setScale(2, RoundingMode.HALF_EVEN);
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
		short increment = Config.withdrawalMultiplierBehavior;

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
			if (multiplierStage < Config.interestMultipliers.size() - 1)
				multiplierStage++;
			
		} else {
			if (remainingOfflineUntilReset > 0)
				remainingOfflineUntilReset--;
			else {
				resetMultiplierStage();
				return 0;
			}
			short increment = Config.offlineMultiplierBehavior;
			int newStage = multiplierStage + increment;
			
			if (newStage < 0) {
				multiplierStage = 0;
			} else if (newStage >= Config.interestMultipliers.size()) {
				multiplierStage = Config.interestMultipliers.size() - 1;
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
			remainingOfflineUntilReset = Config.allowedOfflineBeforeMultiplierReset;
			return true;
		} else {
			if (remainingUntilPayout > 0) {
				if (Config.interestDelayCountWhileOffline)
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
		else if (multiplierStage >= Config.interestMultipliers.size())
			multiplierStage = Config.interestMultipliers.size() - 1;
		
		return Config.interestMultipliers.get(multiplierStage);
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
		else if (stage >= Config.interestMultipliers.size())
			multiplierStage = Config.interestMultipliers.size() - 1;
		else
			multiplierStage = stage;
		return multiplierStage;
	}

	public int setMultiplierStageRelative(int stage) {
		int newStage = multiplierStage + stage;
		if (newStage < 0)
			multiplierStage = 0;
		else if (newStage >= Config.interestMultipliers.size())
			multiplierStage = Config.interestMultipliers.size() - 1;
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

	public BigDecimal getBalance() {
		return balance;
	}
	
	public BigDecimal getPrevBalance() {
		return prevBalance;
	}
	
	/**
	 * Saves the current balance of this account into the previous balance. Used
	 * only at interest payout events. Should only be used AFTER refreshing the
	 * account balance with AccountUtils.appraiseAccountContents() to ensure the
	 * balance is fully up-to-date.
	 */
	public void updatePrevBalance() {
		prevBalance = balance;
	}

	/**
	 * Changes the current balance of this account. Used every time the account
	 * chest is accessed and the contents are changed.
	 * 
	 * @param newBalance the new (positive) balance of the account.
	 */
	public void setBalance(BigDecimal newBalance) {
		if (newBalance != null && newBalance.signum() >= 0)
			this.balance = newBalance.setScale(2, RoundingMode.HALF_EVEN);
	}
}
