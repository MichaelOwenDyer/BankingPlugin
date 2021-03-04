package com.monst.bankingplugin.exceptions;

import org.bukkit.block.Block;

public class ChestBlockedException extends Exception {
    private static final long serialVersionUID = 3718475607700458355L;

    public ChestBlockedException() {
        super();
    }

    public ChestBlockedException(Block b) {
        this(b.getWorld().getName(), b.getX(), b.getY(), b.getZ());
    }

    public ChestBlockedException(String worldName, int x, int y, int z) {
        super(String.format("No space above chest in world \"%s\" at location: %d, %d, %d", worldName, x, y, z));
    }

    public ChestBlockedException(String message) {
        super(message);
    }
}
