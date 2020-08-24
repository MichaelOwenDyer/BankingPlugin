package com.monst.bankingplugin.banking.account;


import com.monst.bankingplugin.banking.bank.BankConfig;
import com.monst.bankingplugin.banking.bank.BankField;

import java.util.List;

/**
 * Defines the current payout status of an {@link Account} and provides
 * arithmetic for determining whether to pay out interest, among other things.
 */
public class AccountStatus {
	
	private final BankConfig bankConfig;

	private int multiplierStage;
	private int delayUntilNextPayout;
	private int remainingOfflinePayouts;
	private int remainingOfflineUntilReset;

	/**
	 * Mint an AccountStatus for a brand new account.
	 */
	public static AccountStatus mint(BankConfig config) {
		return new AccountStatus(
				config,
				0,
				config.get(BankField.INITIAL_INTEREST_DELAY),
				config.get(BankField.ALLOWED_OFFLINE_PAYOUTS),
				config.get(BankField.ALLOWED_OFFLINE_PAYOUTS_BEFORE_RESET)
		);
	}

	/**
	 * Creates an account status with the given values.
	 * @param config The BankConfig from the account's bank
	 * @param multiplierStage The current multiplier stage of the account
	 * @param delayUntilNextPayout The initial delay value
	 * @param remainingOfflinePayouts How many offline payouts are currently remaining
	 * @param remainingOfflinePayoutsBeforeReset How many offline payouts are currently remaining until multiplier reset
	 */
	public AccountStatus(BankConfig config, int multiplierStage, int delayUntilNextPayout,
						 int remainingOfflinePayouts, int remainingOfflinePayoutsBeforeReset) {
		this.bankConfig = config;
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
		int decrement = bankConfig.get(BankField.WITHDRAWAL_MULTIPLIER_DECREMENT);
		if (decrement < 0) {
			resetMultiplierStage();
			return 0;
		}
		return multiplierStage = Math.max(multiplierStage - decrement, 0);
	}

	/**
	 * Increments this account's multiplier stage.
	 * @param online whether the player is online or offline
	 */
	public void incrementMultiplier(boolean online) {
		List<Integer> multipliers = bankConfig.get(BankField.MULTIPLIERS);

		if (online) {
			multiplierStage = Math.max(multiplierStage++, multipliers.size() - 1);
			resetRemainingOfflineUntilReset();
		} else {
			if (mustResetMultiplier())
				resetMultiplierStage();
			int newStage = multiplierStage + (int) bankConfig.get(BankField.OFFLINE_MULTIPLIER_DECREMENT);
			multiplierStage = Math.max(0, Math.min(newStage, multipliers.size() - 1));
		}
	}

	private void resetRemainingOfflineUntilReset() {
		remainingOfflineUntilReset = bankConfig.get(BankField.ALLOWED_OFFLINE_PAYOUTS_BEFORE_RESET);
	}

	private boolean mustResetMultiplier() {
		if (remainingOfflineUntilReset <= 0)
			return true;
		remainingOfflineUntilReset--;
		return false;
	}

	/**
	 * Resets this account's multiplier stage to zero, the first index of the config list.
	 */
	private void resetMultiplierStage() {
		multiplierStage = 0;
	}
	
	/**
	 * Determines whether to allow the next interest payout or not.
	 * 
	 * @param online whether the player is online or not
	 * @return True if interest payout is allowed, or false if not.
	 */
	public boolean allowNextPayout(boolean online) {
		if (mustWait(online))
			return false;
		return canGenerateAgainOffline();
	}

	private boolean mustWait(boolean online) {
		if (delayUntilNextPayout <= 0)
			return false;
		if (online || (boolean) bankConfig.get(BankField.COUNT_INTEREST_DELAY_OFFLINE))
			delayUntilNextPayout--;
		return true;
	}

	private boolean canGenerateAgainOffline() {
		if (remainingOfflinePayouts <= 0)
			return false;
		remainingOfflinePayouts--;
		return true;
	}
	
	/**
	 * Gets the multiplier from Config:interestMultipliers corresponding to this account's current multiplier stage.
	 * @return the corresponding multiplier, or 1x by default in case of an error.
	 */
	public int getRealMultiplier() {
		List<Integer> multipliers = bankConfig.get(BankField.MULTIPLIERS);
		if (multipliers == null || multipliers.isEmpty())
			return 1;
		return multipliers.get(Math.max(0, Math.min(multiplierStage, multipliers.size() - 1)));
	}
	
	public int setMultiplierStage(int stage) {
		List<Integer> multipliers = bankConfig.get(BankField.MULTIPLIERS);
		return multiplierStage = Math.max(0, Math.min(stage, multipliers.size() - 1));
	}

	public int setMultiplierStageRelative(int stage) {
		return setMultiplierStage(multiplierStage + stage);
	}

	public int setInterestDelay(int delay) {
		return delayUntilNextPayout = Math.max(0, delay);
	}

	public int setInterestDelayRelative(int delay) {
		return setInterestDelay(delayUntilNextPayout + delay);
	}
}
