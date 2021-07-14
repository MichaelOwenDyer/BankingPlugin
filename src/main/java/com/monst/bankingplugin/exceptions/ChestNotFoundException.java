package com.monst.bankingplugin.exceptions;

import com.monst.bankingplugin.geo.locations.AccountLocation;

public class ChestNotFoundException extends NotFoundException {

    public ChestNotFoundException(AccountLocation loc) {
        super("chest", loc);
    }

}
