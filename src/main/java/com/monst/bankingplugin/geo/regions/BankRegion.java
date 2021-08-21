package com.monst.bankingplugin.geo.regions;

import com.monst.bankingplugin.geo.Vector2D;
import com.monst.bankingplugin.geo.locations.AccountLocation;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.*;

public abstract class BankRegion {

	final World world;

	BankRegion(World world) {
		this.world = world;
	}

	/**
	 * @return the point on the bounding box of this {@link BankRegion} and the lowest x, y, and z values.
	 */
    public Block getMinimumBlock() {
		return world.getBlockAt(getMinX(), getMinY(), getMinZ());
	}

	/**
	 * @return the point on the bounding box of this {@link BankRegion} and the highest x, y, and z values.
	 */
	public Block getMaximumBlock() {
		return world.getBlockAt(getMaxX(), getMaxY(), getMaxZ());
	}

	/**
	 * Gets the center point of this region.
	 *
	 * @return the center point
	 */
	public abstract Block getCenterPoint();

	public Location getTeleportLocation() {
		return Utils.getSafeBlock(getCenterPoint()).getLocation().add(0.5, 0, 0.5);
	}

	public Location getHighestTeleportLocation() {
		return world.getHighestBlockAt(getCenterPoint().getLocation()).getLocation().add(0.5, 1.0, 0.5);
	}

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
	 * Gets a {@link String} that illustrates the location of this region.
	 *
	 * @return a coordinate string
	 */
	public abstract String getCoordinates();

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

	/**
	 * Gets a {@link Set<Vector2D>} containing a horizontal cross-section
	 * of this region and no y-coordinate.
	 *
	 * @return a set and every {@link Vector2D} in this region
	 */
	public Set<Vector2D> getFootprint() {
		Set<Vector2D> blocks = new HashSet<>();
		int maxX = getMaxX();
		int maxZ = getMaxZ();
		for (int x = getMinX(); x <= maxX; x++) {
			for (int z = getMinZ(); z <= maxZ; z++) {
				if (contains(x, z))
					blocks.add(new Vector2D(x, z));
			}
		}
		return blocks;
	}

	/**
	 * Get all (upper and lower) corner {@link Block}s of this region.
	 *
	 * @return a {@link List<Block>} representing all corner {@link Block}s.
	 */
	public abstract List<Location> getCorners();

	public boolean isCuboid() {
		return false;
	}

	public boolean isPolygonal() {
		return false;
	}

	public boolean isCylindrical() {
		return false;
	}

}
