package com.monst.bankingplugin.selections;

import java.awt.Shape;
import java.awt.geom.Area;
import java.util.Collection;

import org.bukkit.Location;
import org.bukkit.World;

public interface Selection {

	enum SelectionType {
		CUBOID, POLYGONAL
	}

	/**
	 * Get the lower point of a selection.
	 * 
	 * @return The minimum point
	 */
    Location getMinimumPoint();

	/**
	 * Get the upper point of a selection.
	 * 
	 * @return The maximum point
	 */
    Location getMaximumPoint();

	/**
	 * Get the world.
	 * 
	 * @return
	 */
    World getWorld();

	/**
	 * Get a coordinate string that defines the location of the selection.
	 * 
	 * @return A coordinate string.
	 */
    String getCoordinates();

	/**
	 * Get the number of blocks in the selection.
	 * 
	 * @return number of blocks
	 */
    int getVolume();
	
	/**
	 * Get whether or not this selection overlaps with another one.
	 * @param sel
	 * @return
	 */
	default boolean overlaps(Selection sel) {
		Area area = new Area(getShape());
		area.intersect(new Area(sel.getShape()));
		return !area.isEmpty();
	}
	
	/**
	 * Get the shape associated with this selection.
	 * @return
	 */
    Shape getShape();

	/**
	 * Get all vertices of the selection.
	 * 
	 * @return a Collection<Location> representing all vertices.
	 */
    Collection<Location> getVertices();

	/**
	 * Returns true based on whether the selection contains the point,
	 *
	 * @param pt
	 * @return
	 */
    boolean contains(Location pt);

	/**
	 * Returns the type of selection.
	 * 
	 * @return SelectionType.CUBOID or SelectionType.POLYGONAL
	 */
    SelectionType getType();

}
