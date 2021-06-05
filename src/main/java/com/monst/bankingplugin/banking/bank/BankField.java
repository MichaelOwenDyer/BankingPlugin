package com.monst.bankingplugin.banking.bank;

import java.util.Locale;
import java.util.stream.Stream;

public enum BankField {

    NAME ("Name", true),
    OWNER ("OwnerUUID", true),
    COUNT_INTEREST_DELAY_OFFLINE ("CountInterestDelayOffline"),
    REIMBURSE_ACCOUNT_CREATION ("ReimburseAccountCreation"),
    PAY_ON_LOW_BALANCE ("PayOnLowBalance"),
    INTEREST_RATE ("InterestRate"),
    ACCOUNT_CREATION_PRICE ("AccountCreationPrice"),
    MINIMUM_BALANCE ("MinimumBalance"),
    LOW_BALANCE_FEE ("LowBalanceFee"),
    INITIAL_INTEREST_DELAY ("InitialInterestDelay"),
    ALLOWED_OFFLINE_PAYOUTS ("AllowedOfflinePayouts"),
    ALLOWED_OFFLINE_PAYOUTS_BEFORE_MULTIPLIER_RESET ("AllowedOfflinePayoutsBeforeMultiplierReset"),
    OFFLINE_MULTIPLIER_DECREMENT ("OfflineMultiplierDecrement"),
    WITHDRAWAL_MULTIPLIER_DECREMENT ("WithdrawalMultiplierDecrement"),
    PLAYER_BANK_ACCOUNT_LIMIT ("PlayerBankAccountLimit"),
    MULTIPLIERS ("Multipliers"),
    INTEREST_PAYOUT_TIMES ("InterestPayoutTimes"),
    WORLD ("World", true),
    MIN_X ("MinX", true),
    MAX_X ("MaxX", true),
    MIN_Y ("MinY", true),
    MAX_Y ("MaxY", true),
    MIN_Z ("MinZ", true),
    MAX_Z ("MaxZ", true),
    POLYGON_VERTICES ("PolygonVertices", true),
    SELECTION ("", true); // Meant as a placeholder for the previous 7

    private static final BankField[] VALUES = values();
    private static final BankField[] CONFIGURABLE = stream().filter(BankField::isConfigurable).toArray(BankField[]::new);

    private final String databaseName;
    private final boolean databaseOnly;

    BankField(String databaseName) {
        this(databaseName, false);
    }

    BankField(String databaseName, boolean databaseOnly) {
        this.databaseName = databaseName;
        this.databaseOnly = databaseOnly;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public static BankField getByName(String name) {
        return stream()
                .filter(field -> field.toString().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    public boolean isConfigurable() {
        return !databaseOnly;
    }

    public static Stream<BankField> stream() {
        return Stream.of(VALUES);
    }

    public static Stream<BankField> streamConfigurable() {
        return Stream.of(CONFIGURABLE);
    }

    @Override
    public String toString() {
        return name().toLowerCase(Locale.ROOT).replace('_', '-');
    }

}
