package com.monst.bankingplugin.geo.locations;

import com.monst.bankingplugin.geo.BlockVector3D;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.Objects;

public class DoubleChestLocation extends ChestLocation {

    public static DoubleChestLocation from(World world, BlockVector3D v1, BlockVector3D v2) {
        if (!v1.isAdjacent(v2))
            throw new IllegalArgumentException("Blocks must be adjacent!");
        return new DoubleChestLocation(world, v1, v2);
    }

    final BlockVector3D v2;

    DoubleChestLocation(World world, BlockVector3D v1, BlockVector3D v2) {
        super(world, Utils.lesser(v1, v2));
        this.v2 = Utils.greater(v1, v2);
    }

    public BlockVector3D getMaximumBlock() {
        return v2;
    }

    public Location getMaximumLocation() {
        return v2.toLocation(world);
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

    public SingleChestLocation contract(BlockVector3D v1) {
        return new SingleChestLocation(world, v1);
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
        return Objects.equals(world, otherLoc.world) && Objects.equals(v1, otherLoc.v1) && Objects.equals(v2, otherLoc.v2);
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
