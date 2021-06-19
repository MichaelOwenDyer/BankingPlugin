package com.monst.bankingplugin.geo.locations;

import com.monst.bankingplugin.exceptions.ChestNotFoundException;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.InventoryHolder;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;

public class DoubleChestLocation extends ChestLocation {

    private final BlockFace direction;
    private final Block b2;

    public DoubleChestLocation(@Nonnull Block block, @Nonnull BlockFace direction) {
        super(block);
        this.direction = direction;
        this.b2 = b1.getRelative(direction);
    }

    @Override
    public Block getMinimumBlock() {
        switch (direction) {
            case SOUTH:
            case EAST:
                return b1;
            case NORTH:
            case WEST:
                return b2;
        }
        throw new IllegalStateException("Illegal BlockFace direction!");
    }

    @Override
    public Block getMaximumBlock() {
        switch (direction) {
            case NORTH:
            case WEST:
                return b1;
            case SOUTH:
            case EAST:
                return b2;
        }
        throw new IllegalStateException("Illegal BlockFace direction!");
    }

    @Override
    public InventoryHolder findInventoryHolder() throws ChestNotFoundException {
        InventoryHolder ih1 = getInventoryHolderAt(b1);
        InventoryHolder ih2 = getInventoryHolderAt(b2);
        if (ih1 != null && ih2 != null && isSameChest(ih1, ih2))
            return ih1;
        throw new ChestNotFoundException(this);
    }

    private boolean isSameChest(InventoryHolder ih1, InventoryHolder ih2) {
        Location loc1 = ih1.getInventory().getLocation();
        Location loc2 = ih2.getInventory().getLocation();
        return loc1 != null && loc2 != null && Objects.equals(loc1.getBlock(), loc2.getBlock());
    }

    @Override
    public Iterator<Block> iterator() {
        return Arrays.asList(getMinimumBlock(), getMaximumBlock()).iterator();
    }

    @Override
    public Location getTeleportLocation() {
        switch (direction) {
            case NORTH:
            case SOUTH:
                return new Location(getWorld(), b1.getX() + 0.5, b1.getY() + 1, getMaximumBlock().getZ());
            case EAST:
            case WEST:
                return new Location(getWorld(), getMaximumBlock().getX(), b1.getY() + 1, b1.getZ() + 0.5);
        }
        return null;
    }

    @Override
    public byte getSize() {
        return 2;
    }

    public SingleChestLocation contract(Block leaveBehind) {
        if (Objects.equals(b1, leaveBehind))
            return new SingleChestLocation(b2);
        if (Objects.equals(b2, leaveBehind))
            return new SingleChestLocation(b1);
        throw new IllegalArgumentException("Block not contained in ChestLocation cannot be removed!");
    }

    public String toString() {
        return "(" +
                b1.getX() + ", " +
                b1.getY() + ", " +
                b1.getZ() +
                "), (" +
                b2.getX() + ", " +
                b2.getY() + ", " +
                b2.getZ() +
                ")";
    }

}
