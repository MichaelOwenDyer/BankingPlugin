package com.monst.bankingplugin.selections;

import org.bukkit.Location;
import org.bukkit.World;

import java.util.Collection;
import java.util.Set;

public interface Selection {

	enum SelectionType {
		CUBOID, POLYGONAL
	}

	/**
	 * @return the point on the bounding box of this {@link Selection} with the lowest x, y, and z values.
	 */
    BlockVector3D getMinimumPoint();

	/**
	 * @return the point on the bounding box of this {@link Selection} with the highest x, y, and z values.
	 */
    BlockVector3D getMaximumPoint();

	/**
	 * Gets the center point of this selection.
	 *
	 * @return the center point
	 */
	BlockVector3D getCenterPoint();

	/**
	 * Gets the lowest y-coordinate encompassed by this selection.
	 *
	 * @return the minimum y
	 */
	int getMinY();

	/**
	 * Gets the highest y-coordinate encompassed by this selection.
	 *
	 * @return the maximum y
	 */
	int getMaxY();

	/**
	 * Get the world.
	 * 
	 * @return World
	 */
    World getWorld();

	/**
	 * Gets a coordinate string that defines the location of this selection.
	 * 
	 * @return A coordinate string.
	 */
    String getCoordinates();

	/**
	 * Gets the number of blocks in this selection.
	 * 
	 * @return number of blocks
	 */
    long getVolume();
	
	/**
	 * Checks whether or not this selection overlaps with another one.
	 * @param sel The other selection
	 * @return Yes or no
	 */
	boolean overlaps(Selection sel);

	/**
	 * Returns true based on whether this selection contains the {@link Location},
	 *
	 * @param loc The location that may or may not be contained by this selection
	 * @return Whether or not the location is contained
	 */
	boolean contains(Location loc);

	boolean contains(BlockVector3D bv);

	boolean contains(BlockVector2D bv);

	/**
	 * Gets a {@link Set} with a {@link BlockVector2D} for every block in this selection,
	 * disregarding the y-coordinate.
	 *
	 * @return a set with every {@link BlockVector2D} in this selection
	 */
	Set<BlockVector2D> getBlocks();
	
	/**
	 * Get all vertices of the selection.
	 * 
	 * @return a Collection<Location> representing all vertices.
	 */
    Collection<BlockVector3D> getVertices();

	/**
	 * Returns the type of selection.
	 * 
	 * @return SelectionType.CUBOID or SelectionType.POLYGONAL
	 */
    SelectionType getType();

}
