package com.monst.bankingplugin.geo.selections;

import com.monst.bankingplugin.geo.BlockVector2D;
import com.monst.bankingplugin.geo.locations.ChestLocation;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public abstract class Selection {

	final World world;

	Selection(World world) {
		this.world = world;
	}

	/**
	 * @return the point on the bounding box of this {@link Selection} with the lowest x, y, and z values.
	 */
    public Block getMinimumBlock() {
		return world.getBlockAt(getMinX(), getMinY(), getMinZ());
	}

	/**
	 * @return the point on the bounding box of this {@link Selection} with the highest x, y, and z values.
	 */
	public Block getMaximumBlock() {
		return world.getBlockAt(getMaxX(), getMaxY(), getMaxZ());
	}

	/**
	 * Gets the center point of this selection.
	 *
	 * @return the center point
	 */
	public abstract Block getCenterPoint();

	public Location getTeleportLocation() {
		return Utils.getSafeLocation(getCenterPoint());
	}

	/**
	 * @return the minimum x-coordinate of this {@link Selection}
	 */
	public abstract int getMinX();

	/**
	 * @return the maximum x-coordinate of this {@link Selection}
	 */
	public abstract int getMaxX();

	/**
	 * @return the minimum y-coordinate of this {@link Selection}
	 */
	public abstract int getMinY();

	/**
	 * @return the maximum y-coordinate of this {@link Selection}
	 */
	public abstract int getMaxY();

	/**
	 * @return the minimum z-coordinate of this {@link Selection}
	 */
	public abstract int getMinZ();

	/**
	 * @return the maximum z-coordinate of this {@link Selection}
	 */
	public abstract int getMaxZ();

	/**
	 * @return the world this {@link Selection} is in
	 */
	public World getWorld() {
		return world;
	}

	/**
	 * Gets a {@link String} that illustrates the location of this selection.
	 *
	 * @return a coordinate string
	 */
	public abstract String getCoordinates();

	/**
	 * Gets the number of blocks in this selection.
	 *
	 * @return the number of blocks
	 */
	public abstract long getVolume();

	/**
	 * @param sel The other selection
	 * @return whether or not this selection overlaps with another one
	 */
	public abstract boolean overlaps(Selection sel);

	/**
	 * @param sel The other selection
	 * @return whether this selection *cannot* overlap with the other selection
	 */
	public final boolean isDisjunct(Selection sel) {
		return getMinX() > sel.getMaxX() || getMaxX() < sel.getMinX() ||
				getMinY() > sel.getMaxY() || getMaxY() < sel.getMinY() ||
				getMinZ() > sel.getMaxZ() || getMaxZ() < sel.getMinZ();
	}

	public boolean contains(ChestLocation chest) {
		for (Block chestSide : chest)
			if (!contains(chestSide))
				return false;
		return true;
	}

	/**
	 * Returns true based on whether this selection contains the {@link Block},
	 *
	 * @param block The block that may or may not be contained by this selection
	 * @return Whether or not the block is contained
	 */
	public boolean contains(Block block) {
		if (!Objects.equals(getWorld(), block.getWorld()))
			return false;
		return contains(block.getX(), block.getY(), block.getZ());
	}

	/**
	 * Returns true if this selection contains this set of coordinates, assuming the same {@link World}.
	 *
	 * @param x the x-coordinate
	 * @param y the y-coordinate
	 * @param z the z-coordinate
	 * @return Whether or not the set of coordinates is contained
	 */
	public boolean contains(int x, int y, int z) {
		return y <= getMaxY() && y >= getMinY() && contains(x, z);
	}

	/**
	 * Returns true if this selection contains this (x,z) coordinate pair, assuming the same {@link World} and a compatible y-coordinate.
	 *
	 * @param x the x-coordinate
	 * @param z the z-coordinate
	 * @return Whether or not the coordinate pair is contained
	 */
	public abstract boolean contains(int x, int z);

	/**
	 * Gets a {@link Set<BlockVector2D>} containing a horizontal cross-section
	 * of this selection with no y-coordinate.
	 *
	 * @return a set with every {@link BlockVector2D} in this selection
	 */
	public abstract Set<BlockVector2D> getFootprint();

	/**
	 * Gets an ordered list of {@link BlockVector2D}s containing each vertex of this selection.
	 * The order of the elements is selection-specific.
	 * @return a {@link List<BlockVector2D>} of all vertices of the selection.
	 */
	public abstract List<BlockVector2D> getVertices();

	/**
	 * Get all (upper and lower) corner {@link Block}s of this selection.
	 * This will return two blocks per {@link BlockVector2D} from {@link #getVertices()},
	 * one at {@link #getMinY()} and the other at {@link #getMaxY()}.
	 *
	 * @return a {@link List<Block>} representing all corner {@link Block}s.
	 */
	public List<Block> getCorners() {
		List<Block> vertices = new LinkedList<>();
		for (BlockVector2D bv : getVertices()) {
			vertices.add(world.getBlockAt(bv.getX(), getMinY(), bv.getZ()));
			vertices.add(world.getBlockAt(bv.getX(), getMaxY(), bv.getZ()));
		}
		return vertices;
	}

	public boolean isCuboid() {
		return false;
	}

	public boolean isPolygonal() {
		return false;
	}

}
