package com.monst.bankingplugin.geo.locations;

import com.monst.bankingplugin.exceptions.ChestNotFoundException;
import com.monst.bankingplugin.geo.BlockVector3D;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.inventory.Inventory;

import java.util.Objects;

class DoubleChestLocation extends ChestLocation {

    final BlockVector3D v2;

    DoubleChestLocation(World world, BlockVector3D v1, BlockVector3D v2) {
        super(world, v1);
        this.v2 = v2;
    }

    public BlockVector3D getMaximumBlock() {
        return v2;
    }

    public Location getMaximumLocation() {
        return v2.toLocation(world);
    }

    @Override
    public Inventory findInventory() throws ChestNotFoundException {
        Block b1 = getMinimumLocation().getBlock();
        Block b2 = getMaximumLocation().getBlock();
        Inventory inv1 = Utils.getChestAt(b1).getInventory();
        Inventory inv2 = Utils.getChestAt(b2).getInventory();
        if (!Objects.equals(inv1, inv2))
            throw new ChestNotFoundException(this);
        return inv1;
    }

    @Override
    public Location getTeleportLocation() {
        if (v1.getX() == v2.getX())
            return new Location(world, v1.getX() + 0.5, v1.getY(), v2.getZ());
        if (v1.getZ() == v2.getZ())
            return new Location(world, v2.getX(), v1.getY(), v1.getZ() + 0.5);
        return null;
    }

    @Override
    public Location[] getLocations() {
        return new Location[] { v1.toLocation(world), v2.toLocation(world) };
    }

    @Override
    public byte getSize() {
        return 2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        DoubleChestLocation otherLoc = (DoubleChestLocation) o;
        return world.equals(otherLoc.world) && v1.equals(otherLoc.v1) && v2.equals(otherLoc.v2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(world, v1, v2);
    }

    @Override
    public String toString() {
        return String.format("DoubleChestLocation {%d,%d,%d and %d,%d,%d}",
                v1.getX(), v1.getY(), v1.getZ(),
                v2.getX(), v2.getY(), v2.getZ()
        );
    }

}
