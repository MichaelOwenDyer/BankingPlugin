package com.monst.bankingplugin.banking.account;


import com.monst.bankingplugin.banking.bank.Bank;
import com.monst.bankingplugin.banking.bank.BankField;

import java.util.List;

/**
 * Defines the current payout status of an {@link Account} and provides
 * arithmetic for determining whether to pay out interest, among other things.
 */
public class AccountStatus {
	
	private final Bank bank;

	private int multiplierStage;
	private int delayUntilNextPayout;
	private int remainingOfflinePayouts;
	private int remainingOfflineUntilReset;

	/**
	 * Mint an AccountStatus for a brand new account.
	 */
	public static AccountStatus mint(Bank bank) {
		return new AccountStatus(
				bank,
				0,
				bank.get(BankField.INITIAL_INTEREST_DELAY),
				bank.get(BankField.ALLOWED_OFFLINE_PAYOUTS),
				bank.get(BankField.ALLOWED_OFFLINE_PAYOUTS_BEFORE_RESET)
		);
	}

	/**
	 * Creates an account status with the given values.
	 * @param bank The account's bank
	 * @param multiplierStage The current multiplier stage of the account
	 * @param delayUntilNextPayout The initial delay value
	 * @param remainingOfflinePayouts How many offline payouts are currently remaining
	 * @param remainingOfflinePayoutsBeforeReset How many offline payouts are currently remaining until multiplier reset
	 */
	public AccountStatus(Bank bank, int multiplierStage, int delayUntilNextPayout,
						 int remainingOfflinePayouts, int remainingOfflinePayoutsBeforeReset) {
		this.bank = bank;
		this.multiplierStage = multiplierStage;
		this.delayUntilNextPayout = delayUntilNextPayout;
		this.remainingOfflinePayouts = remainingOfflinePayouts;
		this.remainingOfflineUntilReset = remainingOfflinePayoutsBeforeReset;
	}

	/**
	 * Gets the current multiplier stage of this account. This is an index on the list of multipliers specified by the bank.
	 * @return the current multiplier stage
	 */
	public int getMultiplierStage() {
		return multiplierStage;
	}

	/**
	 * Gets the interest delay at this account. This specifies how many interest cycles this account will skip before
	 * starting to generate interest (again).
	 *
	 * @return the current delay, in cycles
	 */
	public int getDelayUntilNextPayout() {
		return delayUntilNextPayout;
	}

	/**
	 * Gets the current number of remaining offline payouts at this account. This specifies how many (more) consecutive
	 * times this account will generate interest for offline account holders.
	 *
	 * @return the current number of remaining offline payouts
	 */
	public int getRemainingOfflinePayouts() {
		return remainingOfflinePayouts;
	}

	/**
	 * Gets the current number of remaining offline payouts until the multiplier resets at this account. This specifies
	 * how many (more) consecutive times this account will generate interest for offline account holders before the
	 * multiplier stage is reset to 0.
	 *
	 * @return the current number of remaining offline payouts until the multiplier is reset
	 */
	public int getRemainingOfflineUntilReset() {
		return remainingOfflineUntilReset;
	}

	/**
	 * Processes a withdrawal at this account. This is only triggered when the balance of the account drops below
	 * the balance at the previous interest payout. The account multiplier is reduced by the amount specified
	 * at the bank.
	 *
	 * @return the new multiplier stage of this account
	 */
	public int processWithdrawal() {
		int decrement = bank.get(BankField.WITHDRAWAL_MULTIPLIER_DECREMENT);
		if (decrement < 0) {
			resetMultiplierStage();
			return 0;
		}
		return setMultiplierStage(multiplierStage - decrement);
	}

	/**
	 * Increments this account's multiplier stage.
	 *
	 * @param online whether any account holder is online
	 */
	public void incrementMultiplier(boolean online) {
		List<Integer> multipliers = bank.get(BankField.MULTIPLIERS);
		if (online) {
			multiplierStage = Math.max(multiplierStage++, multipliers.size() - 1);
			resetRemainingOfflineUntilReset();
		} else {
			if (mustResetMultiplier())
				resetMultiplierStage();
			int newStage = multiplierStage + (int) bank.get(BankField.OFFLINE_MULTIPLIER_DECREMENT);
			multiplierStage = Math.max(0, Math.min(newStage, multipliers.size() - 1));
		}
	}

	/**
	 * Resets the allowed offline payouts until reset to the value specified by the associated bank.
	 * This determines how many consecutive cycles an account will generate interest for offline account holders until
	 * the account multiplier is reset.
	 */
	private void resetRemainingOfflineUntilReset() {
		remainingOfflineUntilReset = bank.get(BankField.ALLOWED_OFFLINE_PAYOUTS_BEFORE_RESET);
	}

	/**
	 * Determines whether or not the multiplier of the associated account needs to be reset.
	 *
	 * @return true if the multiplier needs to be reset, false if not
	 */
	private boolean mustResetMultiplier() {
		if (remainingOfflineUntilReset <= 0)
			return true;
		remainingOfflineUntilReset--;
		return false;
	}

	/**
	 * Resets this account's multiplier stage to zero, the first index of the multiplier list.
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

	/**
	 * Determines whether the associated account must wait before generating interest, e.g. if the interest delay > 0.
	 * Also decrements the counter if the account must wait.
	 *
	 * @param online whether or not any account holder is online
	 * @return true if the account must wait, false if a payout is allowed
	 */
	private boolean mustWait(boolean online) {
		if (delayUntilNextPayout <= 0)
			return false;
		if (online || (boolean) bank.get(BankField.COUNT_INTEREST_DELAY_OFFLINE))
			delayUntilNextPayout--;
		return true;
	}

	/**
	 * Determines whether the associated account can generate interest can generate interest (again) while all
	 * account holders are offline. Also decrements the counter if a payout is allowed.
	 *
	 * @return true if a payout is allowed, false if not
	 */
	private boolean canGenerateAgainOffline() {
		if (remainingOfflinePayouts <= 0)
			return false;
		remainingOfflinePayouts--;
		return true;
	}
	
	/**
	 * Gets the multiplier from Config:interestMultipliers corresponding to this account's current multiplier stage.
	 *
	 * @return the corresponding multiplier, or 1x by default in case of an error.
	 */
	public int getRealMultiplier() {
		List<Integer> multipliers = bank.get(BankField.MULTIPLIERS);
		if (multipliers == null || multipliers.isEmpty())
			return 1;
		return multipliers.get(setMultiplierStage(multiplierStage));
	}

	/**
	 * Sets the multiplier stage. This will ensure that the provided stage is no less than 0 and no greater than multipliers.size() - 1.
	 *
	 * @param stage the stage to set the multiplier to
	 * @return the new multiplier
	 */
	public int setMultiplierStage(int stage) {
		List<Integer> multipliers = bank.get(BankField.MULTIPLIERS);
		return multiplierStage = Math.max(0, Math.min(stage, multipliers.size() - 1));
	}

	/**
	 * Sets the multiplier stage relative to the current value.
	 *
	 * @param stage the offset from the current stage, positive or negative
	 * @return the new multiplier
	 */
	public int setMultiplierStageRelative(int stage) {
		return setMultiplierStage(multiplierStage + stage);
	}

	/**
	 * Sets the interest delay. This determines how long an account must wait before generating interest.
	 *
	 * @param delay the delay to set
	 */
	public void setInterestDelay(int delay) {
		delayUntilNextPayout = Math.max(0, delay);
	}

}
