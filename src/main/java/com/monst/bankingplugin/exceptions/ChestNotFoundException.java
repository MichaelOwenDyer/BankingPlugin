package com.monst.bankingplugin.exceptions;

import com.monst.bankingplugin.geo.locations.AccountLocation;

public class ChestNotFoundException extends NotFoundException {
    private static final long serialVersionUID = -6446875473671870708L;
    public ChestNotFoundException(AccountLocation loc) {
        super("chest", loc);
    }
}
