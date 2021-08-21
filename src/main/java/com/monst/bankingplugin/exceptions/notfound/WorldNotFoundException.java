package com.monst.bankingplugin.exceptions.notfound;

public class WorldNotFoundException extends NotFoundException {

    public WorldNotFoundException(String worldName) {
        super("Could not find world with name \"" + worldName + "\"");
    }

}
