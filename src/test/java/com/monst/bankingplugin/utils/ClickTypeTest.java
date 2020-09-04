package com.monst.bankingplugin.utils;

import com.monst.bankingplugin.banking.account.Account;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashSet;

class ClickTypeTest {

    @Test
    void get() {
        ClickType<?> clickType = ClickType.migrate(Account.reopen(
                1,
                null,
                new HashSet<>(),
                null,
                null,
                null,
                "Test",
                BigDecimal.TEN,
                BigDecimal.ZERO
        ));
        Account account = clickType.get();
    }
}