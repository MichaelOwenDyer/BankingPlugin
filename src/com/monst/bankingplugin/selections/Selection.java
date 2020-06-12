package com.monst.bankingplugin.selections;

import org.bukkit.Location;
import org.bukkit.World;

public interface Selection {

	/**
	 * Get the lower point of a region.
	 * 
	 * @return min. point
	 */
	public Location getMinimumPoint();

	/**
	 * Get the upper point of a region.
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
	 * Get the number of blocks in the region.
	 * 
	 * @return number of blocks
	 */
	public int getArea();

	/**
	 * Returns true based on whether the region contains the point,
	 *
	 * @param pt
	 * @return
	 */
	public boolean contains(Location pt);

}
