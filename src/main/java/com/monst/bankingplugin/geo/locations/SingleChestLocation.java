package com.monst.bankingplugin.geo.locations;

import com.monst.bankingplugin.exceptions.ChestNotFoundException;
import com.monst.bankingplugin.geo.BlockVector3D;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.inventory.Inventory;

import java.util.Objects;

class SingleChestLocation extends ChestLocation {

    SingleChestLocation(World world, BlockVector3D v1) {
        super(world, v1);
    }

    @Override
    public Inventory findInventory() throws ChestNotFoundException {
        return Utils.getChestAt(getMinimumLocation().getBlock()).getInventory();
    }

    @Override
    public Location getTeleportLocation() {
        return getMinimumLocation().add(0.5, 0.0, 0.5);
    }

    @Override
    public Location[] getLocations() {
        return new Location[] { v1.toLocation(world) };
    }

    @Override
    public byte getSize() {
        return 1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        SingleChestLocation otherLoc = (SingleChestLocation) o;
        return world.equals(otherLoc.world) && v1.equals(otherLoc.v1);
    }

    @Override
    public int hashCode() {
        return Objects.hash(world, v1);
    }

    @Override
    public String toString() {
        return String.format("SingleChestLocation {%d,%d,%d}",
                v1.getX(), v1.getY(), v1.getZ()
        );
    }

}
