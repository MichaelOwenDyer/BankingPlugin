package com.monst.bankingplugin.geo.locations;

import com.monst.bankingplugin.geo.BlockVector3D;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Chest;

import java.util.Objects;

public class SingleChestLocation extends ChestLocation {

    public static SingleChestLocation from(Chest c) {
        return new SingleChestLocation(c.getWorld(), BlockVector3D.fromLocation(c.getLocation()));
    }

    public static SingleChestLocation from(World world, BlockVector3D v1) {
        return new SingleChestLocation(world, v1);
    }

    SingleChestLocation(World world, BlockVector3D v1) {
        super(world, v1);
    }

    @Override
    public Location getTeleportLocation() {
        return getMinimumLocation().add(0.5, 0.0, 0.5);
    }

    @Override
    public Location[] getLocations() {
        return new Location[] { v1.toLocation(world) };
    }

    public DoubleChestLocation extend(BlockVector3D v2) {
        return DoubleChestLocation.from(world, v1, v2);
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
        return Objects.equals(world, otherLoc.world) && Objects.equals(v1, otherLoc.v1);
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
