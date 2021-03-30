package com.monst.bankingplugin.banking.bank;

import com.monst.bankingplugin.config.Config;
import org.apache.commons.lang.WordUtils;

import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Represents all configuration fields at a {@link Bank}.
 */
public enum BankField {

    COUNT_INTEREST_DELAY_OFFLINE (() -> Config.countInterestDelayOffline.isOverridable()),
    REIMBURSE_ACCOUNT_CREATION (() -> Config.reimburseAccountCreation.isOverridable()),
    PAY_ON_LOW_BALANCE (() -> Config.payOnLowBalance.isOverridable()),
    INTEREST_RATE (() -> Config.interestRate.isOverridable()),
    ACCOUNT_CREATION_PRICE (() -> Config.accountCreationPrice.isOverridable()),
    MINIMUM_BALANCE (() -> Config.minimumBalance.isOverridable()),
    LOW_BALANCE_FEE (() -> Config.lowBalanceFee.isOverridable()),
    INITIAL_INTEREST_DELAY (() -> Config.initialInterestDelay.isOverridable()),
    ALLOWED_OFFLINE_PAYOUTS (() -> Config.allowedOfflinePayouts.isOverridable()),
    ALLOWED_OFFLINE_PAYOUTS_BEFORE_MULTIPLIER_RESET(() -> Config.allowedOfflinePayoutsBeforeReset.isOverridable()),
    OFFLINE_MULTIPLIER_DECREMENT (() -> Config.offlineMultiplierDecrement.isOverridable()),
    WITHDRAWAL_MULTIPLIER_DECREMENT (() -> Config.withdrawalMultiplierDecrement.isOverridable()),
    PLAYER_BANK_ACCOUNT_LIMIT (() -> Config.playerBankAccountLimit.isOverridable()),
    MULTIPLIERS (() -> Config.multipliers.isOverridable()),
    INTEREST_PAYOUT_TIMES (() -> Config.interestPayoutTimes.isOverridable());

    private final Supplier<Boolean> overridable;
    private final String configName;
    private final String databaseName;

    BankField(Supplier<Boolean> configPair) {
        this.overridable = configPair;
        this.configName = toString().toLowerCase().replace("_", "-");
        this.databaseName = WordUtils.capitalizeFully(toString(), new char[] {'_'}).replace("_", "");
    }

    /**
     * @return the name of this field
     */
    public String getConfigName() {
        return configName;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public boolean isOverridable() {
        return overridable.get();
    }

    /**
     * @param name the name of the field
     * @return the field with the specified name
     */
    public static BankField getByName(String name) {
        return Stream.of(values())
                .filter(field -> field.getConfigName().equalsIgnoreCase(name) ||
                        field.getDatabaseName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

}
