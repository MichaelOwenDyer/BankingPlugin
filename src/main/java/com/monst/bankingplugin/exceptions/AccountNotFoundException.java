package com.monst.bankingplugin.exceptions;

import com.monst.bankingplugin.geo.locations.ChestLocation;
import org.bukkit.block.Block;

public class AccountNotFoundException extends NotFoundException {
    private static final long serialVersionUID = -6446875937564270708L;
    public AccountNotFoundException(Block b) {
        super("account", b);
    }
    public AccountNotFoundException(ChestLocation loc) {
        super("chest", loc);
    }
}
