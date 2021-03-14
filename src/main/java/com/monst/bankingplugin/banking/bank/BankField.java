package com.monst.bankingplugin.banking.bank;

import com.monst.bankingplugin.config.Config;
import org.apache.commons.lang.WordUtils;

import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Represents all configuration fields at a {@link Bank}.
 */
public enum BankField {

    COUNT_INTEREST_DELAY_OFFLINE (() -> Config.getCountInterestDelayOffline().isOverridable()),
    REIMBURSE_ACCOUNT_CREATION (() -> Config.getReimburseAccountCreation().isOverridable()),
    PAY_ON_LOW_BALANCE (() -> Config.getPayOnLowBalance().isOverridable()),
    INTEREST_RATE (() -> Config.getInterestRate().isOverridable()),
    ACCOUNT_CREATION_PRICE (() -> Config.getAccountCreationPrice().isOverridable()),
    MINIMUM_BALANCE (() -> Config.getMinimumBalance().isOverridable()),
    LOW_BALANCE_FEE (() -> Config.getLowBalanceFee().isOverridable()),
    INITIAL_INTEREST_DELAY (() -> Config.getInitialInterestDelay().isOverridable()),
    ALLOWED_OFFLINE_PAYOUTS (() -> Config.getAllowedOfflinePayouts().isOverridable()),
    ALLOWED_OFFLINE_PAYOUTS_BEFORE_MULTIPLIER_RESET(() -> Config.getAllowedOfflinePayoutsBeforeReset().isOverridable()),
    OFFLINE_MULTIPLIER_DECREMENT (() -> Config.getOfflineMultiplierDecrement().isOverridable()),
    WITHDRAWAL_MULTIPLIER_DECREMENT (() -> Config.getWithdrawalMultiplierDecrement().isOverridable()),
    PLAYER_BANK_ACCOUNT_LIMIT (() -> Config.getPlayerBankAccountLimit().isOverridable()),
    MULTIPLIERS (() -> Config.getMultipliers().isOverridable()),
    INTEREST_PAYOUT_TIMES (() -> Config.getInterestPayoutTimes().isOverridable());

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
