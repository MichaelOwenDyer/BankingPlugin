package com.monst.bankingplugin.entity.geo.location;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.InventoryHolder;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;

public class DoubleAccountLocation extends AccountLocation {

    private final int maxX;
    private final int maxZ;

    public DoubleAccountLocation(Block block, BlockFace direction) {
        this(block.getWorld(), block.getX(), block.getY(), block.getZ(), block.getRelative(direction).getX(), block.getRelative(direction).getZ());
    }
    
    DoubleAccountLocation(World world, int x1, int y, int z1, int x2, int z2) {
        super(world, Math.min(x1, x2), y, Math.min(z1, z2));
        this.maxX = Math.max(x1, x2);
        this.maxZ = Math.max(z1, z2);
    }
    
    @Override
    public int getMaxX() {
        return maxX;
    }
    
    @Override
    public int getMaxZ() {
        return maxZ;
    }
    
    @Override
    public Optional<InventoryHolder> findChest() {
        InventoryHolder ih1 = getChestAt(getMinimumBlock());
        InventoryHolder ih2 = getChestAt(getMaximumBlock());
        if (ih1 == null || ih2 == null || !isSameChest(ih1, ih2))
            return Optional.empty();
        return Optional.of(ih1);
    }

    private boolean isSameChest(InventoryHolder ih1, InventoryHolder ih2) {
        Location loc1 = ih1.getInventory().getLocation();
        Location loc2 = ih2.getInventory().getLocation();
        return Objects.equals(loc1, loc2);
    }

    @Override
    public Iterator<Block> iterator() {
        return Arrays.asList(getMinimumBlock(), getMaximumBlock()).iterator();
    }

    @Override
    public Location getTeleportLocation() {
        if (minX == maxX)
            return getMaximumBlock().getLocation().add(0.5, 1, 0);
        return getMaximumBlock().getLocation().add(0, 1, 0.5);
    }

    public AccountLocation contract(Block block) {
        if (block.getX() == minX && block.getZ() == minZ)
            return new AccountLocation(world, maxX, y, maxZ);
        if (block.getX() == maxX && block.getZ() == maxZ)
            return new AccountLocation(world, minX, y, minZ);
        throw new IllegalArgumentException("AccountLocation could not be contracted because the specified block was not part of it.");
    }

    @Override
    public byte getSize() {
        return 2;
    }

    public String toString() {
        return "(" +
                minX + ", " +
                y + ", " +
                minZ +
                "), (" +
                maxX + ", " +
                y + ", " +
                maxZ +
                ")";
    }

}
