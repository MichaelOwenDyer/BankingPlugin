package com.monst.bankingplugin.exceptions;

public class WorldNotFoundException extends NotFoundException {

    public WorldNotFoundException(String worldName) {
        super("Could not find world and name \"" + worldName + "\"");
    }

}
