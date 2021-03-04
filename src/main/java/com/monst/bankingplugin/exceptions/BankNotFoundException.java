package com.monst.bankingplugin.exceptions;

import com.monst.bankingplugin.geo.locations.ChestLocation;
import org.bukkit.block.Block;

public class BankNotFoundException extends NotFoundException {
    private static final long serialVersionUID = -6447474733671870708L;
    public BankNotFoundException(Block b) {
        super("bank", b);
    }
    public BankNotFoundException(ChestLocation loc) {
        super("chest", loc);
    }
}
