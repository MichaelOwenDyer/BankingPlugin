package com.monst.bankingplugin.banking.account;

import java.util.function.Function;

public enum AccountField {

    OWNER ("OwnerUUID", Account::getOwnerUUID),
    BANK ("BankID", a -> a.getBank().getID()),
    WORLD ("World", a -> a.getChestLocation().getWorld()),
    Y ("Y", a -> a.getChestLocation().getMinimumBlock().getY()),
    X1 ("X1", a -> a.getChestLocation().getMinimumBlock().getX()),
    Z1 ("Z1", a -> a.getChestLocation().getMinimumBlock().getZ()),
    X2 ("X2", a -> a.getChestLocation().getMaximumBlock().getX()),
    Z2 ("Z2", a -> a.getChestLocation().getMaximumBlock().getZ()),
    LOCATION ("", null), // Meant as a placeholder for the previous 6
    NICKNAME ("Nickname", Account::getRawName),
    BALANCE ("Balance", Account::getBalance),
    PREVIOUS_BALANCE ("PreviousBalance", Account::getPrevBalance),
    MULTIPLIER ("MultiplierStage", Account::getMultiplierStage),
    DELAY_UNTIL_NEXT_PAYOUT ("DelayUntilNextPayout", Account::getDelayUntilNextPayout),
    REMAINING_OFFLINE_PAYOUTS ("RemainingOfflinePayouts", Account::getRemainingOfflinePayouts),
    REMAINING_OFFLINE_PAYOUTS_UNTIL_RESET ("RemainingOfflinePayoutsUntilReset", Account::getRemainingOfflinePayoutsUntilReset);

    private final String attribute;
    private final Function<Account, Object> getter;

    AccountField(String attributes, Function<Account, Object> getter) {
        this.attribute = attributes;
        this.getter = getter;
    }

    public String getName() {
        return attribute;
    }

    public Object getFrom(Account account) {
        return getter.apply(account);
    }

}
