package com.monst.bankingplugin.selections;

import java.awt.Shape;
import java.awt.geom.Area;
import java.util.Collection;

import org.bukkit.Location;
import org.bukkit.World;

public interface Selection {

	public enum SelectionType {
		CUBOID, POLYGONAL
	}

	/**
	 * Get the lower point of a selection.
	 * 
	 * @return min. point
	 */
	public Location getMinimumPoint();

	/**
	 * Get the upper point of a selection.
	 * 
	 * @return max. point
	 */
	public Location getMaximumPoint();

	/**
	 * Get the world.
	 * 
	 * @return
	 */
	public World getWorld();

	/**
	 * Get the number of blocks in the selection.
	 * 
	 * @return number of blocks
	 */
	public int getVolume();
	
	/**
	 * Get whether or not this selection overlaps with another one.
	 * @param sel
	 * @return
	 */
	public default boolean overlaps(Selection sel) {
		Area area = new Area(getShape());
		area.intersect(new Area(sel.getShape()));
		return !area.isEmpty();
	}
	
	/**
	 * Get the shape associated with this selection.
	 * @return
	 */
	public Shape getShape();

	/**
	 * Get all vertices of the selection.
	 * 
	 * @return a Collection<Location> representing all vertices.
	 */
	public Collection<Location> getVertices();

	/**
	 * Returns true based on whether the selection contains the point,
	 *
	 * @param pt
	 * @return
	 */
	public boolean contains(Location pt);

	/**
	 * Returns the type of selection.
	 * 
	 * @return SelectionType.CUBOID or SelectionType.POLYGONAL
	 */
	public SelectionType getType();

}
