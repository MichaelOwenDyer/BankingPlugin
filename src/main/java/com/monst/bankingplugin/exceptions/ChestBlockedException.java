package com.monst.bankingplugin.exceptions;

import org.bukkit.block.Block;

public class ChestBlockedException extends Exception {

    public ChestBlockedException(Block b) {
        this(b.getWorld().getName(), b.getX(), b.getY(), b.getZ());
    }

    public ChestBlockedException(String worldName, int x, int y, int z) {
        super(String.format("No space above chest in world \"%s\" at location: %d, %d, %d", worldName, x, y, z));
    }

}
