package com.monst.bankingplugin.exceptions;

import com.monst.bankingplugin.lang.Message;

public class BankNotFoundException extends NotFoundException {

    public BankNotFoundException() {
        super(Message.CHEST_NOT_IN_BANK.translate());
    }

}
