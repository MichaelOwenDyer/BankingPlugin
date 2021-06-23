package com.monst.bankingplugin.banking;

import java.util.Locale;
import java.util.function.Function;
import java.util.stream.Stream;

public enum AccountField implements BankingEntityField<Account> {

    OWNER ("OwnerUUID", Account::getOwnerUUID),
    BANK ("BankID", a -> a.getBank().getID()),
    WORLD ("World", a -> a.getLocation().getWorld().getName()),
    Y ("Y", a -> a.getLocation().getMinimumBlock().getY()),
    X1 ("X1", a -> a.getLocation().getMinimumBlock().getX()),
    Z1 ("Z1", a -> a.getLocation().getMinimumBlock().getZ()),
    X2 ("X2", a -> a.getLocation().getMaximumBlock().getX()),
    Z2 ("Z2", a -> a.getLocation().getMaximumBlock().getZ()),
    LOCATION ("", null), // Meant as a placeholder for the previous 6
    NICKNAME ("Nickname", Account::getRawName),
    BALANCE ("Balance", Account::getBalance),
    PREVIOUS_BALANCE ("PreviousBalance", Account::getPrevBalance),
    MULTIPLIER_STAGE ("MultiplierStage", Account::getMultiplierStage),
    DELAY_UNTIL_NEXT_PAYOUT ("DelayUntilNextPayout", Account::getDelayUntilNextPayout),
    REMAINING_OFFLINE_PAYOUTS ("RemainingOfflinePayouts", Account::getRemainingOfflinePayouts),
    REMAINING_OFFLINE_PAYOUTS_UNTIL_RESET ("RemainingOfflinePayoutsUntilReset", Account::getRemainingOfflinePayoutsUntilReset);

    private static final AccountField[] VALUES = values();

    private final String databaseAttribute;
    private final Function<Account, Object> getter;

    AccountField(String attributes, Function<Account, Object> getter) {
        this.databaseAttribute = attributes;
        this.getter = getter;
    }

    public String getDatabaseAttribute() {
        return databaseAttribute;
    }

    @Override
    public Object getFrom(Account account) {
        return getter.apply(account);
    }

    public static AccountField getByName(String name) {
        return stream()
                .filter(field -> field.toString().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    public static Stream<AccountField> stream() {
        return Stream.of(VALUES);
    }

    @Override
    public String toString() {
        return name().toLowerCase(Locale.ROOT).replace('_', '-');
    }

}
