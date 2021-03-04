package com.monst.bankingplugin.exceptions;

import com.monst.bankingplugin.geo.locations.ChestLocation;
import org.bukkit.block.Block;

public class ChestNotFoundException extends NotFoundException {
    private static final long serialVersionUID = -6446875473671870708L;
    public ChestNotFoundException(Block b) {
        super("chest", b);
    }
    public ChestNotFoundException(ChestLocation loc) {
        super("chest", loc);
    }
}
