package com.monst.bankingplugin.command;

import org.bukkit.permissions.Permissible;

@FunctionalInterface
public interface Permission {
    
    boolean ownedBy(Permissible permissible);
    
    default boolean notOwnedBy(Permissible permissible) {
        return !ownedBy(permissible);
    }
    
    default Permission and(Permission other) {
        return permissible -> ownedBy(permissible) && other.ownedBy(permissible);
    }
    
    default Permission or(Permission other) {
        return permissible -> ownedBy(permissible) || other.ownedBy(permissible);
    }
    
    static Permission none() {
        return permissible -> true;
    }
    
    static Permission having(String permissionNode) {
        return permissible -> permissible.hasPermission(permissionNode);
    }
    
}
