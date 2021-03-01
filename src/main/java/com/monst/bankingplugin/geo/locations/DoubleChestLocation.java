package com.monst.bankingplugin.geo.locations;

import com.monst.bankingplugin.geo.BlockVector2D;
import com.monst.bankingplugin.geo.BlockVector3D;
import org.bukkit.Location;
import org.bukkit.World;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Iterator;

class DoubleChestLocation extends ChestLocation {

    private BlockVector2D v2;

    DoubleChestLocation(World world, BlockVector3D v1, BlockVector2D v2) {
        super(world, v1);
        this.v2 = v2;
    }

    public Location getMaximumLocation() {
        return v2.toLocation(world, v1.getY());
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
    @Nonnull
    public Iterator<Location> iterator() {
        return Arrays.asList(v1.toLocation(world), v2.toLocation(world, v1.getY())).iterator();
    }

    @Override
    public byte getSize() {
        return 2;
    }

}
