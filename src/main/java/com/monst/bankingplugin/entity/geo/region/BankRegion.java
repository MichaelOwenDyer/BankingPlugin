package com.monst.bankingplugin.entity.geo.region;

import com.monst.bankingplugin.converter.WorldConverter;
import com.monst.bankingplugin.entity.AbstractEntity;
import com.monst.bankingplugin.entity.geo.Vector2;
import com.monst.bankingplugin.entity.geo.Vector3;
import com.monst.bankingplugin.entity.geo.location.AccountLocation;
import jakarta.persistence.*;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "bank_region")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "shape")
public abstract class BankRegion extends AbstractEntity {

    @Convert(converter = WorldConverter.class)
    World world;

    public BankRegion() {}

    BankRegion(World world) {
        this.generateID();
        this.world = world;
    }

    /**
     * Gets the center point of this region.
     *
     * @return the center point
     */
    public abstract Vector3 getCenterPoint();

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
    public abstract boolean overlaps(BankRegion region);

    public final boolean overlapsX(int minX, int maxX) {
        return maxX >= getMinX() && minX <= getMaxX();
    }

    public final boolean overlapsY(int minY, int maxY) {
        return maxY >= getMinY() && minY <= getMaxY();
    }

    public final boolean overlapsZ(int minZ, int maxZ) {
        return maxZ >= getMinZ() && minZ <= getMaxZ();
    }

    /**
     * @param region The other region
     * @return whether this region *cannot* overlap with the other region
     */
    public final boolean isDisjunct(BankRegion region) {
        return  !overlapsX(region.getMinX(), region.getMaxX()) &&
                !overlapsY(region.getMinY(), region.getMaxY()) &&
                !overlapsZ(region.getMinZ(), region.getMaxZ());
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

    public boolean contains(Vector3 v) {
        return contains(v.getX(), v.getY(), v.getZ());
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

    public boolean contains(Vector2 v) {
        return contains(v.getX(), v.getZ());
    }

    /**
     * Returns true if this region contains this (x,z) coordinate pair, assuming the same {@link World} and a compatible y-coordinate.
     *
     * @param x the x-coordinate
     * @param z the z-coordinate
     * @return Whether or not the coordinate pair is contained
     */
    public abstract boolean contains(int x, int z);

    /**
     * Gets a {@link Set <Vector2>} containing a horizontal cross-section
     * of this region and no y-coordinate.
     *
     * @return a set and every {@link Vector2} in this region
     */
    public Set<Vector2> getFootprint() {
        Set<Vector2> blocks = new HashSet<>();
        int maxX = getMaxX();
        int maxZ = getMaxZ();
        for (int x = getMinX(); x <= maxX; x++) {
            for (int z = getMinZ(); z <= maxZ; z++) {
                if (contains(x, z))
                    blocks.add(new Vector2(x, z));
            }
        }
        return blocks;
    }

    /**
     * Get all (upper and lower) corner {@link Block}s of this region.
     *
     * @return a {@link List<Block>} representing all corner {@link Block}s.
     */
    public abstract List<Block> getCorners();

    public Location getTeleportLocation() {
        return getSafeBlock(getCenterPoint().toBlock(world)).getLocation().add(0.5, 0, 0.5);
    }

    public Location getHighestTeleportLocation() {
        return world.getHighestBlockAt(getCenterPoint().toLocation(world)).getLocation().add(0.5, 1.0, 0.5);
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
        for (Block block = start; start.getY() > 0; block = block.getRelative(BlockFace.DOWN))
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
