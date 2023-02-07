package com.monst.bankingplugin.entity.geo.region;

import com.monst.bankingplugin.entity.Entity;
import com.monst.bankingplugin.entity.geo.location.AccountLocation;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.awt.*;
import java.awt.geom.Area;
import java.util.List;
import java.util.Objects;

public abstract class BankRegion extends Entity {

    final World world;

    public static BankRegion fromDatabase(World world, Integer x1, int y1, Integer z1, Integer x2, int y2, Integer z2,
                                          int[] pointsX, int[] pointsZ) {
        if (x1 == null || z1 == null || x2 == null || z2 == null)
            return new PolygonalBankRegion(world, pointsX, pointsZ, y1, y2);
        return new CuboidBankRegion(world, x1, y1, z1, x2, y2, z2);
    }

    BankRegion(World world) {
        this.world = world;
    }

    /**
     * Gets the center point of this region.
     *
     * @return the center point
     */
    public abstract Block getCenterBlock();

    /**
     * @return the minimum x-coordinate of this {@link BankRegion}
     */
    public abstract int getMinX();

    /**
     * @return the maximum x-coordinate of this {@link BankRegion}
     */
    public abstract int getMaxX();

    /**
     * @return the minimum y-coordinate of this {@link BankRegion}
     */
    public abstract int getMinY();

    /**
     * @return the maximum y-coordinate of this {@link BankRegion}
     */
    public abstract int getMaxY();

    /**
     * @return the minimum z-coordinate of this {@link BankRegion}
     */
    public abstract int getMinZ();

    /**
     * @return the maximum z-coordinate of this {@link BankRegion}
     */
    public abstract int getMaxZ();

    /**
     * @return the world this {@link BankRegion} is in
     */
    public World getWorld() {
        return world;
    }

    public int getLength() {
        return getMaxX() - getMinX() + 1;
    }

    public int getWidth() {
        return getMaxZ() - getMinZ() + 1;
    }

    public int getHeight() {
        return getMaxY() - getMinY() + 1;
    }

    /**
     * Gets the number of blocks in this region.
     *
     * @return the number of blocks
     */
    public abstract long getVolume();

    /**
     * @param region The other region
     * @return whether or not this region overlaps and another one
     */
    public boolean overlaps(BankRegion region) {
        Area area = new Area(getShape());
        area.intersect(new Area(region.getShape()));
        return !area.isEmpty();
    }

    public final boolean overlapsX(int minX, int maxX) {
        return !(maxX < getMinX() || minX > getMaxX());
    }

    public final boolean overlapsY(int minY, int maxY) {
        return !(maxY < getMinY() || minY > getMaxY());
    }
    
    public final boolean overlapsZ(int minZ, int maxZ) {
        return !(maxZ < getMinZ() || minZ > getMaxZ());
    }

    public boolean contains(AccountLocation chest) {
        if (!Objects.equals(world, chest.getWorld()))
            return false;
        for (Block chestSide : chest)
            if (!contains(chestSide))
                return false;
        return true;
    }

    /**
     * Returns true based on whether this region contains the {@link Block},
     *
     * @param block The block that may or may not be contained by this region
     * @return Whether or not the block is contained
     */
    public boolean contains(Block block) {
        if (!Objects.equals(getWorld(), block.getWorld()))
            return false;
        return contains(block.getX(), block.getY(), block.getZ());
    }

    /**
     * Returns true if this region contains this set of coordinates, assuming the same {@link World}.
     *
     * @param x the x-coordinate
     * @param y the y-coordinate
     * @param z the z-coordinate
     * @return Whether or not the set of coordinates is contained
     */
    public boolean contains(int x, int y, int z) {
        return overlapsY(y, y) && contains(x, z);
    }

    /**
     * Returns true if this region contains this (x,z) coordinate pair, assuming the same {@link World} and a compatible y-coordinate.
     *
     * @param x the x-coordinate
     * @param z the z-coordinate
     * @return Whether or not the coordinate pair is contained
     */
    public abstract boolean contains(int x, int z);
    
    @SuppressWarnings("unused")
    abstract Shape getShape(); // TODO: Use this as a replacement for getFootprint()?

    /**
     * Get all (upper and lower) corner {@link Block}s of this region.
     *
     * @return a {@link List<Block>} representing all corner {@link Block}s.
     */
    public abstract List<Block> getCorners();

    public Location getTeleportLocation() {
        return getSafeBlock(getCenterBlock()).getLocation().add(0.5, 0, 0.5);
    }

    public Location getHighestTeleportLocation() {
        return world.getHighestBlockAt(getCenterBlock().getLocation()).getLocation().add(0.5, 1.0, 0.5);
    }

    public boolean isCuboid() {
        return false;
    }

    public boolean isPolygonal() {
        return false;
    }

    /**
     * Gets a {@link String} that illustrates the location of this region.
     *
     * @return a coordinate string
     */
    @Override
    public abstract String toString();

    /**
     * Finds the next lowest safe {@link Block} at or directly below a certain {@link Block}.
     * If no safe block is found then the original block is returned.
     * @param start the block from which to start searching
     * @return a {@link Block} at or directly below the given block that is safe to stand on
     */
    static Block getSafeBlock(Block start) {
        // Check all the way down to -64 to support later versions of Minecraft
        for (Block block = start; block.getY() > -64; block = block.getRelative(BlockFace.DOWN))
            if (isSafeBlock(block))
                return block;
        return start;
    }

    /**
     * Checks if a {@link Block} is safe to stand on (solid ground with 2 breathable blocks)
     *
     * @param block Block to check
     * @return true if block is safe
     */
    @SuppressWarnings("deprecation")
    static boolean isSafeBlock(Block block) {
        if (!block.getType().isTransparent())
            return false; // not transparent (standing in block)
        Block blockAbove = block.getRelative(BlockFace.UP);
        if (!blockAbove.getType().isTransparent())
            return false; // not transparent (will suffocate)
        Block blockBelow = block.getRelative(BlockFace.DOWN);
        return blockBelow.getType().isSolid() || blockBelow.getType() == Material.WATER;
    }

}
