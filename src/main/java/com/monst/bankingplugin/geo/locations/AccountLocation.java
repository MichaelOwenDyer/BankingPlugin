package com.monst.bankingplugin.geo.locations;

import com.monst.bankingplugin.geo.BlockVector2D;
import com.monst.bankingplugin.geo.BlockVector3D;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.inventory.InventoryHolder;

public abstract class AccountLocation {

    public static AccountLocation from(InventoryHolder holder) {
        Location[] locations = Utils.getChestLocations(holder);
        if (locations.length > 1)
            return from(locations[0], locations[1]);
        return from(locations[0]);
    }

    public static AccountLocation from(Location loc) {
        if (loc.getWorld() == null)
            throw new IllegalArgumentException("World must not be null!");
        return of(loc.getWorld(), BlockVector3D.fromLocation(loc));
    }

    public static AccountLocation from(Location loc1, Location loc2) {
        if (loc1.getWorld() == null)
            throw new IllegalArgumentException("World must not be null!");
        if (loc1.getBlockY() != loc2.getBlockY())
            throw new IllegalArgumentException("y-coordinates must be equal!");
        return of(loc1.getWorld(), BlockVector3D.fromLocation(loc1), BlockVector2D.fromLocation(loc2));
    }

    public static AccountLocation of(World world, BlockVector3D v1) {
        return new SingleAccountLocation(world, v1);
    }

    public static AccountLocation of(World world, BlockVector3D v1, BlockVector2D v2) {
        BlockVector3D bv3 = v2.toBlockVector3D(v1.getY());
        if (v1.equals(bv3))
            return of(world, v1);
        if (!v1.isAdjacent(bv3))
            throw new IllegalArgumentException("Blocks must be adjacent to one another!");
        return new DoubleAccountLocation(world, Utils.lesser(v1, bv3), Utils.greater(v1.toBlockVector2D(), v2));
    }

    World world;
    BlockVector3D v1;

    AccountLocation(World world, BlockVector3D v1) {
        this.world = world;
        this.v1 = v1;
    }

    public Location getMinimumLocation() {
        return v1.toLocation(world);
    }

    public abstract Location getAverageLocation();
}
