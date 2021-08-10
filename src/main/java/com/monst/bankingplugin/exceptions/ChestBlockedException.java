package com.monst.bankingplugin.exceptions;

import com.monst.bankingplugin.lang.Message;

public class ChestBlockedException extends Exception {

    public ChestBlockedException() {
        super(Message.CHEST_BLOCKED.translate());
    }

}
