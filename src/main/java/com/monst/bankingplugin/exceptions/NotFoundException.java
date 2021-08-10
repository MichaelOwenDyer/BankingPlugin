package com.monst.bankingplugin.exceptions;

import com.monst.bankingplugin.geo.locations.AccountLocation;

public abstract class NotFoundException extends Exception {
    public NotFoundException(String message) {
        super(message);
    }
    public NotFoundException(String type, AccountLocation loc) {
        super(String.format("No %s found in world \"%s\" at location: %s", type, loc.getWorld().getName(), loc));
    }
}
