package com.monst.bankingplugin.banking;

import java.util.EnumSet;
import java.util.Locale;
import java.util.function.Function;
import java.util.stream.Stream;

public enum AccountField implements BankingEntityField<Account> {

    OWNER ("OwnerUUID", Account::getOwnerUUID),
    BANK ("BankID", account -> account.getBank().getID()),
    NICKNAME ("Nickname", Account::getRawName),
    BALANCE ("Balance", Account::getBalance),
    PREVIOUS_BALANCE ("PreviousBalance", Account::getPreviousBalance),
    MULTIPLIER_STAGE ("MultiplierStage", Account::getMultiplierStage),
    DELAY_UNTIL_NEXT_PAYOUT ("DelayUntilNextPayout", Account::getDelayUntilNextPayout),
    REMAINING_OFFLINE_PAYOUTS ("RemainingOfflinePayouts", Account::getRemainingOfflinePayouts),
    WORLD ("World", account -> account.getLocation().getWorld().getName()),
    Y ("Y", account -> account.getLocation().getY()),
    X1 ("X1", account -> account.getLocation().getMinimumBlock().getX()),
    Z1 ("Z1", account -> account.getLocation().getMinimumBlock().getZ()),
    X2 ("X2", account -> account.getLocation().getMaximumBlock().getX()),
    Z2 ("Z2", account -> account.getLocation().getMaximumBlock().getZ()),
    LOCATION ("", account -> {
        throw new UnsupportedOperationException();
    });

    private static final EnumSet<AccountField> VALUES = EnumSet.complementOf(EnumSet.of(LOCATION));

    private final String path;
    private final String databaseAttribute;
    private final Function<Account, Object> getter;

    AccountField(String attributes, Function<Account, Object> getter) {
        this.path = name().toLowerCase(Locale.ROOT).replace('_', '-');
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

    public static Stream<AccountField> stream() {
        return VALUES.stream();
    }

    @Override
    public String toString() {
        return path;
    }

}
