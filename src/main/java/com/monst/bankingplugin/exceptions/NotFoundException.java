package com.monst.bankingplugin.exceptions;

import com.monst.bankingplugin.geo.locations.ChestLocation;
import org.bukkit.block.Block;

public class NotFoundException extends Exception {
    public NotFoundException() {
        super();
    }
    public NotFoundException(String type, Block b) {
        super(String.format("No " + type + " found in world \"%s\" at location: %d, %d, %d",
                b.getWorld().getName(), b.getX(), b.getY(), b.getZ()));
    }
    public NotFoundException(String type, ChestLocation loc) {
        super(String.format("No " + type + " found in world \"%s\" at location: ",
                loc.getWorld().getName(), loc.toString()));
    }
}
