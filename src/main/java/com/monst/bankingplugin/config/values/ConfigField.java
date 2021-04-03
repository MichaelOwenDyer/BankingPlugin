package com.monst.bankingplugin.config.values;

import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.config.values.overridable.OverridableValue;
import com.monst.bankingplugin.config.values.simple.SimpleValue;

import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
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
    REIMBURSE_ACCOUNT_CREATION (Config.reimburseAccountCreation),
    PAY_ON_LOW_BALANCE (Config.payOnLowBalance),
    INTEREST_RATE (Config.interestRate),
    ACCOUNT_CREATION_PRICE (Config.accountCreationPrice),
    MINIMUM_BALANCE (Config.minimumBalance),
    LOW_BALANCE_FEE (Config.lowBalanceFee),
    INITIAL_INTEREST_DELAY (Config.initialInterestDelay),
    ALLOWED_OFFLINE_PAYOUTS (Config.allowedOfflinePayouts),
    ALLOWED_OFFLINE_PAYOUTS_BEFORE_MULTIPLIER_RESET (Config.allowedOfflinePayoutsBeforeReset),
    OFFLINE_MULTIPLIER_DECREMENT (Config.offlineMultiplierDecrement),
    WITHDRAWAL_MULTIPLIER_DECREMENT (Config.withdrawalMultiplierDecrement),
    PLAYER_BANK_ACCOUNT_LIMIT (Config.playerBankAccountLimit),
    MULTIPLIERS (Config.multipliers),
    INTEREST_PAYOUT_TIMES (Config.interestPayoutTimes);

    private final ConfigValue<?> configValue;
    private final boolean isSimple;

    ConfigField(SimpleValue<?> simpleValue) {
        this.configValue = simpleValue;
        this.isSimple = true;
    }

    ConfigField(OverridableValue<?> overridableValue) {
        this.configValue = overridableValue;
        this.isSimple = false;
    }

    public ConfigValue<?> getConfigValue() {
        return configValue;
    }

    public SimpleValue<?> getSimpleValue() {
        if (isSimple)
            return (SimpleValue<?>) configValue;
        return null;
    }

    public OverridableValue<?> getOverridableValue() {
        if (!isSimple)
            return (OverridableValue<?>) configValue;
        return null;
    }

    public boolean isSimple() {
        return isSimple;
    }

    public boolean isOverridable() {
        return !isSimple;
    }

    public static ConfigField getByName(String name) {
        String beforeDot = name.split("\\.", 2)[0];
        return Stream.of(values())
                .filter(field -> field.toString().equalsIgnoreCase(beforeDot))
                .findFirst()
                .orElse(null);
    }

    public static Set<ConfigField> getSimpleFields() {
        return Stream.of(values())
                .filter(ConfigField::isSimple)
                .collect(Collectors.toSet());
    }

    public static Set<ConfigField> getOverridableFields() {
        return Stream.of(values())
                .filter(ConfigField::isOverridable)
                .collect(Collectors.toSet());
    }

    @Override
    public String toString() {
        return super.toString().toLowerCase(Locale.ROOT).replace("_", "-");
    }

}
