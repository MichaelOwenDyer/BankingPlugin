package com.monst.bankingplugin.entity.geo.location;

import com.monst.bankingplugin.entity.Entity;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.inventory.InventoryHolder;

import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;

public class AccountLocation extends Entity implements Iterable<Block> {
    
    public static AccountLocation fromDatabase(World world, int x1, int y, int z1, Integer x2, Integer z2) {
        if (x2 == null || z2 == null)
            return new AccountLocation(world, x1, y, z1);
        return new DoubleAccountLocation(world, x1, y, z1, x2, z2);
    }

    public static AccountLocation from(InventoryHolder ih) {
        if (ih instanceof DoubleChest) {
            DoubleChest dc = (DoubleChest) ih;
            Chest left = (Chest) dc.getLeftSide();
            Chest right = (Chest) dc.getRightSide();
            return new DoubleAccountLocation(dc.getWorld(), left.getX(), left.getY(), left.getZ(), right.getX(), right.getZ());
        }
        if (ih instanceof Chest) {
            Chest chest = (Chest) ih;
            return new AccountLocation(chest.getWorld(), chest.getX(), chest.getY(), chest.getZ());
        }
        throw new IllegalArgumentException("InventoryHolder must be a chest!");
    }
    
    final World world;
    final int minX;
    final int y;
    final int minZ;

    AccountLocation(World world, int minX, int y, int minZ) {
        this.world = world;
        this.minX = minX;
        this.y = y;
        this.minZ = minZ;
    }
    
    public Block getMinimumBlock() {
        return world.getBlockAt(getMinX(), getY(), getMinZ());
    }

    public Block getMaximumBlock() {
        return world.getBlockAt(getMaxX(), getY(), getMaxZ());
    }
    
    public World getWorld() {
        return world;
    }
    
    public int getMinX() {
        return minX;
    }
    
    public int getY() {
        return y;
    }
    
    public int getMinZ() {
        return minZ;
    }
    
    public int getMaxX() {
        return minX;
    }
    
    public int getMaxZ() {
        return minZ;
    }

    public Optional<InventoryHolder> findChest() {
        return Optional.ofNullable(getChestAt(getMinimumBlock()));
    }

    InventoryHolder getChestAt(Block block) {
        if (block.getWorld() == null)
            return null;
        if (block.getType() != Material.CHEST && block.getType() != Material.TRAPPED_CHEST)
            return null;
        return ((Chest) block.getState()).getInventory().getHolder();
    }
    
    @Override
    public Iterator<Block> iterator() {
        return Collections.singleton(getMinimumBlock()).iterator();
    }
    
    public Location getTeleportLocation() {
        return getMinimumBlock().getLocation().add(0.5, 1, 0.5);
    }

    public byte getSize() {
        return 1;
    }
    
    @Override
    public String toString() {
        return "(" +
                minX + ", " +
                y + ", " +
                minZ +
                ")";
    }

}
