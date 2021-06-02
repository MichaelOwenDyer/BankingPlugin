package com.monst.bankingplugin.config.values;

import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.config.values.overridable.OverridableValue;
import com.monst.bankingplugin.config.values.simple.AllowOverride;

import java.util.Locale;
import java.util.stream.Stream;

public enum ConfigField {

    ACCOUNT_COMMAND_NAME (Config.accountCommandName),
    BANK_COMMAND_NAME (Config.bankCommandName),
    CONTROL_COMMAND_NAME (Config.controlCommandName),
    ACCOUNT_INFO_ITEM (Config.accountInfoItem),
    BANK_CREATION_PRICE (Config.bankCreationPrice),
    REIMBURSE_BANK_CREATION (Config.reimburseBankCreation),
    DEFAULT_BANK_LIMIT (Config.defaultBankLimit),
    DEFAULT_ACCOUNT_LIMIT (Config.defaultAccountLimit),
    MINIMUM_BANK_VOLUME (Config.minimumBankVolume),
    MAXIMUM_BANK_VOLUME (Config.maximumBankVolume),
    STUBBORN_BANKS (Config.stubbornBanks),
    ALLOW_SELF_BANKING (Config.allowSelfBanking),
    CONFIRM_ON_REMOVE (Config.confirmOnRemove),
    CONFIRM_ON_REMOVE_ALL (Config.confirmOnRemoveAll),
    CONFIRM_ON_TRANSFER (Config.confirmOnTransfer),
    TRUST_ON_TRANSFER (Config.trustOnTransfer),
    ENABLE_UPDATE_CHECKER (Config.enableUpdateChecker),
    ENABLE_ACCOUNT_TRANSACTION_LOG (Config.enableAccountTransactionLog),
    ENABLE_ACCOUNT_INTEREST_LOG (Config.enableAccountInterestLog),
    ENABLE_BANK_PROFIT_LOG (Config.enableBankProfitLog),
    ENABLE_DEBUG_LOG (Config.enableDebugLog),
    CLEANUP_LOG_DAYS (Config.cleanupLogDays),
    ENABLE_WORLDGUARD_INTEGRATION (Config.enableWorldGuardIntegration),
    ENABLE_GRIEFPREVENTION_INTEGRATION (Config.enableGriefPreventionIntegration),
    ENABLE_WORLDEDIT_INTEGRATION (Config.enableWorldEditIntegration),
    WORLDGUARD_DEFAULT_FLAG_VALUE (Config.worldGuardDefaultFlagValue),
    REMOVE_ACCOUNT_ON_ERROR (Config.removeAccountOnError),
    BLACKLIST (Config.blacklist),
    BANK_REVENUE_FUNCTION (Config.bankRevenueFunction),
    DISABLED_WORLDS (Config.disabledWorlds),
    ENABLE_MAIL (Config.enableMail),
    LANGUAGE_FILE (Config.languageFile),
    NAME_REGEX (Config.nameRegex),

    COUNT_INTEREST_DELAY_OFFLINE (Config.countInterestDelayOffline),
    COUNT_INTEREST_DELAY_OFFLINE$ALLOW_OVERRIDE (Config.countInterestDelayOffline.getAllowOverride()),
    REIMBURSE_ACCOUNT_CREATION (Config.reimburseAccountCreation),
    REIMBURSE_ACCOUNT_CREATION$ALLOW_OVERRIDE (Config.reimburseAccountCreation.getAllowOverride()),
    PAY_ON_LOW_BALANCE (Config.payOnLowBalance),
    PAY_ON_LOW_BALANCE$ALLOW_OVERRIDE (Config.payOnLowBalance.getAllowOverride()),
    INTEREST_RATE (Config.interestRate),
    INTEREST_RATE$ALLOW_OVERRIDE (Config.interestRate.getAllowOverride()),
    ACCOUNT_CREATION_PRICE (Config.accountCreationPrice),
    ACCOUNT_CREATION_PRICE$ALLOW_OVERRIDE (Config.accountCreationPrice.getAllowOverride()),
    MINIMUM_BALANCE (Config.minimumBalance),
    MINIMUM_BALANCE$ALLOW_OVERRIDE (Config.minimumBalance.getAllowOverride()),
    LOW_BALANCE_FEE (Config.lowBalanceFee),
    LOW_BALANCE_FEE$ALLOW_OVERRIDE (Config.lowBalanceFee.getAllowOverride()),
    INITIAL_INTEREST_DELAY (Config.initialInterestDelay),
    INITIAL_INTEREST_DELAY$ALLOW_OVERRIDE (Config.initialInterestDelay.getAllowOverride()),
    ALLOWED_OFFLINE_PAYOUTS (Config.allowedOfflinePayouts),
    ALLOWED_OFFLINE_PAYOUTS$ALLOW_OVERRIDE (Config.allowedOfflinePayouts.getAllowOverride()),
    ALLOWED_OFFLINE_PAYOUTS_BEFORE_MULTIPLIER_RESET (Config.allowedOfflinePayoutsBeforeReset),
    ALLOWED_OFFLINE_PAYOUTS_BEFORE_MULTIPLIER_RESET$ALLOW_OVERRIDE (Config.allowedOfflinePayoutsBeforeReset.getAllowOverride()),
    OFFLINE_MULTIPLIER_DECREMENT (Config.offlineMultiplierDecrement),
    OFFLINE_MULTIPLIER_DECREMENT$ALLOW_OVERRIDE (Config.offlineMultiplierDecrement.getAllowOverride()),
    WITHDRAWAL_MULTIPLIER_DECREMENT (Config.withdrawalMultiplierDecrement),
    WITHDRAWAL_MULTIPLIER_DECREMENT$ALLOW_OVERRIDE (Config.withdrawalMultiplierDecrement.getAllowOverride()),
    PLAYER_BANK_ACCOUNT_LIMIT (Config.playerBankAccountLimit),
    PLAYER_BANK_ACCOUNT_LIMIT$ALLOW_OVERRIDE (Config.playerBankAccountLimit.getAllowOverride()),
    MULTIPLIERS (Config.multipliers),
    MULTIPLIERS$ALLOW_OVERRIDE (Config.multipliers.getAllowOverride()),
    INTEREST_PAYOUT_TIMES (Config.interestPayoutTimes),
    INTEREST_PAYOUT_TIMES$ALLOW_OVERRIDE (Config.interestPayoutTimes.getAllowOverride());

    private static final ConfigField[] VALUES = values();

    private final ConfigValue<?> configValue;
    private final boolean isBankField;

    ConfigField(ConfigValue<?> configValue) {
        this.configValue = configValue;
        this.isBankField = false;
    }

    ConfigField(OverridableValue<?> overridableValue) {
        this.configValue = overridableValue;
        this.isBankField = true;
    }

    ConfigField(AllowOverride allowOverride) {
        this.configValue = allowOverride;
        this.isBankField = false;
    }

    public ConfigValue<?> getConfigValue() {
        return configValue;
    }

    public boolean isBankField() {
        return isBankField;
    }

    public static ConfigField getByName(String name) {
        return stream()
                .filter(field -> field.toString().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    public static Stream<ConfigField> stream() {
        return Stream.of(VALUES);
    }

    @Override
    public String toString() {
        return name().toLowerCase(Locale.ROOT).replace('_', '-').replace('$', '.');
    }

}
