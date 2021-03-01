package com.monst.bankingplugin.geo.locations;

import com.monst.bankingplugin.geo.BlockVector2D;
import com.monst.bankingplugin.geo.BlockVector3D;
import com.monst.bankingplugin.geo.selections.Selection;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;

public abstract class ChestLocation implements Iterable<Location> {

    public static ChestLocation from(Block b) {
        Location[] locations = Utils.getChestLocations(((Chest) b.getState()).getInventory().getHolder());
        if (locations.length > 1)
            return from(locations[0], locations[1]);
        return from(locations[0]);
    }

    public static ChestLocation from(Location loc1, Location loc2) {
        World world = loc1.getWorld();
        if (world == null)
            throw new IllegalArgumentException("World must not be null!");
        BlockVector3D v1 = BlockVector3D.fromLocation(loc1);
        BlockVector3D v2 = BlockVector2D.fromLocation(loc2).toBlockVector3D(v1.getY());
        if (!v1.isAdjacent(v2))
            throw new IllegalArgumentException("Blocks must be adjacent to one another!");
        if (v1.equals(v2))
            return new SingleChestLocation(world, v1);
        return new DoubleChestLocation(world, Utils.lesser(v1, v2), Utils.greater(v1, v2).toBlockVector2D());
    }

    public static ChestLocation from(Location loc) {
        if (loc.getWorld() == null)
            throw new IllegalArgumentException("World must not be null!");
        return new SingleChestLocation(loc.getWorld(), BlockVector3D.fromLocation(loc));
    }

    World world;
    BlockVector3D v1;

    ChestLocation(World world, BlockVector3D v1) {
        this.world = world;
        this.v1 = v1;
    }

    public Location getMinimumLocation() {
        return v1.toLocation(world);
    }

    public Location getMaximumLocation() {
        return getMinimumLocation();
    }

    public World getWorld() {
        return world;
    }

    public boolean isBlocked() {
        for (Location chest : this)
            if (!Utils.isTransparent(chest.getBlock().getRelative(BlockFace.UP)))
                return false;
        return true;
    }

    public boolean isAt(Location loc) {
        if (!world.equals(loc.getWorld()))
            return false;
        BlockVector3D bv = BlockVector3D.fromLocation(loc);
        for (Location chest : this)
            if (BlockVector3D.fromLocation(chest).equals(bv))
                return true;
        return false;
    }

    public boolean isIn(Selection sel) {
        if (!world.equals(sel.getWorld()))
            return false;
        for (Location chest : this)
            if (!sel.contains(BlockVector3D.fromLocation(chest)))
                return false;
        return true;
    }

    public abstract Location getTeleportLocation();

    public abstract byte getSize();

}
