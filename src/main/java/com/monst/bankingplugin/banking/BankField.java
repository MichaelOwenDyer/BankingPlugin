package com.monst.bankingplugin.banking;

import com.monst.bankingplugin.geo.regions.CylindricalBankRegion;
import com.monst.bankingplugin.geo.regions.PolygonalBankRegion;
import com.monst.bankingplugin.utils.Utils;

import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum BankField implements BankingEntityField<Bank> {

    NAME ("Name", Bank::getRawName),
    OWNER ("OwnerUUID", Bank::getOwnerUUID),
    COUNT_INTEREST_DELAY_OFFLINE ("CountInterestDelayOffline",
            bank -> bank.countInterestDelayOffline().getCustomValue()),
    REIMBURSE_ACCOUNT_CREATION ("ReimburseAccountCreation",
            bank -> bank.reimburseAccountCreation().getCustomValue()),
    PAY_ON_LOW_BALANCE ("PayOnLowBalance",
            bank -> bank.payOnLowBalance().getCustomValue()),
    INTEREST_RATE ("InterestRate",
            bank -> bank.interestRate().getCustomValue()),
    ACCOUNT_CREATION_PRICE ("AccountCreationPrice",
            bank -> bank.accountCreationPrice().getCustomValue()),
    MINIMUM_BALANCE ("MinimumBalance",
            bank -> bank.minimumBalance().getCustomValue()),
    LOW_BALANCE_FEE ("LowBalanceFee",
            bank -> bank.lowBalanceFee().getCustomValue()),
    INITIAL_INTEREST_DELAY ("InitialInterestDelay",
            bank -> bank.initialInterestDelay().getCustomValue()),
    ALLOWED_OFFLINE_PAYOUTS ("AllowedOfflinePayouts",
            bank -> bank.allowedOfflinePayouts().getCustomValue()),
    OFFLINE_MULTIPLIER_DECREMENT ("OfflineMultiplierDecrement",
            bank -> bank.offlineMultiplierDecrement().getCustomValue()),
    WITHDRAWAL_MULTIPLIER_DECREMENT ("WithdrawalMultiplierDecrement",
            bank -> bank.withdrawalMultiplierDecrement().getCustomValue()),
    PLAYER_BANK_ACCOUNT_LIMIT ("PlayerBankAccountLimit",
            bank -> bank.playerBankAccountLimit().getCustomValue()),
    INTEREST_MULTIPLIERS("Multipliers",
            bank -> bank.multipliers().getCustomValue()),
    INTEREST_PAYOUT_TIMES ("InterestPayoutTimes",
            bank -> bank.interestPayoutTimes().getCustomValue()),
    WORLD ("World", bank -> bank.getRegion().getWorld().getName()),
    MIN_X ("MinX", bank -> bank.getRegion().getMinX()),
    MAX_X ("MaxX", bank -> bank.getRegion().getMaxX()),
    MIN_Y ("MinY", bank -> bank.getRegion().getMinY()),
    MAX_Y ("MaxY", bank -> bank.getRegion().getMaxY()),
    MIN_Z ("MinZ", bank -> bank.getRegion().getMinZ()),
    MAX_Z ("MaxZ", bank -> bank.getRegion().getMaxZ()),
    POLYGON_VERTICES ("PolygonVertices") {
        @Override
        public Object getFrom(Bank bank) {
            return bank.getRegion().isPolygonal() ? ((PolygonalBankRegion) bank.getRegion()).getVertices() : null;
        }
    },
    CYLINDER_CENTER_X ("CylinderCenterX") {
        @Override
        public Object getFrom(Bank bank) {
            return bank.getRegion().isCylindrical() ? ((CylindricalBankRegion) bank.getRegion()).getCenterX() : null;
        }
    },
    CYLINDER_CENTER_Z ("CylinderCenterZ") {
        @Override
        public Object getFrom(Bank bank) {
            return bank.getRegion().isCylindrical() ? ((CylindricalBankRegion) bank.getRegion()).getCenterZ() : null;
        }
    },
    CYLINDER_RADIUS_X ("CylinderRadiusX") {
        @Override
        public Object getFrom(Bank bank) {
            return bank.getRegion().isCylindrical() ? ((CylindricalBankRegion) bank.getRegion()).getRadiusX() : null;
        }
    },
    CYLINDER_RADIUS_Z ("CylinderRadiusZ") {
        @Override
        public Object getFrom(Bank bank) {
            return bank.getRegion().isCylindrical() ? ((CylindricalBankRegion) bank.getRegion()).getRadiusZ() : null;
        }
    },
    REGION ("", bank -> {
        throw new UnsupportedOperationException();
    });

    private static final EnumSet<BankField> VALUES = EnumSet.complementOf(EnumSet.of(REGION));
    private static final EnumSet<BankField> CONFIGURABLE_VALUES = EnumSet.range(COUNT_INTEREST_DELAY_OFFLINE, INTEREST_PAYOUT_TIMES);

    private final String path;
    private final String databaseAttribute;
    private final Function<Bank, Object> getter;

    BankField(String databaseAttribute) {
        this(databaseAttribute, null);
    }

    BankField(String databaseAttribute, Function<Bank, Object> getter) {
        this.path = name().toLowerCase(Locale.ROOT).replace('_', '-');
        this.databaseAttribute = databaseAttribute;
        this.getter = getter;
    }

    public String getDatabaseAttribute() {
        return databaseAttribute;
    }

    @Override
    public Object getFrom(Bank bank) {
        return getter.apply(bank);
    }

    public static Stream<BankField> stream() {
        return VALUES.stream();
    }

    public static List<String> matchConfigurablePath(String input) {
        return CONFIGURABLE_VALUES.stream()
                .map(BankField::toString)
                .filter(path -> Utils.containsIgnoreCase(path, input))
                .sorted()
                .collect(Collectors.toList());
    }

    public static BankField getByName(String input) {
        return CONFIGURABLE_VALUES.stream()
                .filter(f -> f.toString().equalsIgnoreCase(input))
                .findFirst()
                .orElse(null);
    }

    @Override
    public String toString() {
        return path;
    }

}
