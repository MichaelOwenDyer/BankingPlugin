package com.monst.bankingplugin.exceptions;

import com.monst.bankingplugin.geo.locations.AccountLocation;
import org.bukkit.block.Block;

public abstract class NotFoundException extends Exception {
    public NotFoundException(String message) {
        super(message);
    }
    public NotFoundException(String type, Block b) {
        super(String.format("No %s found in world \"%s\" at location: %d, %d, %d",
                type, b.getWorld().getName(), b.getX(), b.getY(), b.getZ()));
    }
    public NotFoundException(String type, AccountLocation loc) {
        super(String.format("No %s found in world \"%s\" at location: %s",
                type, loc.getWorld().getName(), loc));
    }
}
