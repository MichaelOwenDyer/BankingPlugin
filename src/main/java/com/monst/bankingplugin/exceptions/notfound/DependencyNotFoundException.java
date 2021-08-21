package com.monst.bankingplugin.exceptions.notfound;

public class DependencyNotFoundException extends NotFoundException {

    public DependencyNotFoundException(String dependencyName) {
        super("Could not find dependency \"" + dependencyName + "\".");
    }

}
