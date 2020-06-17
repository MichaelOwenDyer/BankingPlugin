package com.monst.bankingplugin.selections;

import org.bukkit.Location;
import org.bukkit.World;

public class CuboidSelection implements Selection {

	private World world;
	private Location min;
	private Location max;

	public CuboidSelection(World world, Location loc1, Location loc2) {
		this.world = world;
		int minX, minY, minZ, maxX, maxY, maxZ;
		if (loc1.getBlockX() < loc2.getBlockX()) {
			minX = loc1.getBlockX();
			maxX = loc2.getBlockX();
		} else {
			minX = loc2.getBlockX();
			maxX = loc1.getBlockX();
		}
		if (loc1.getBlockY() < loc2.getBlockY()) {
			minY = loc1.getBlockY();
			maxY = loc2.getBlockY();
		} else {
			minY = loc2.getBlockY();
			maxY = loc1.getBlockY();
		}
		if (loc1.getBlockZ() < loc2.getBlockZ()) {
			minZ = loc1.getBlockZ();
			maxZ = loc2.getBlockZ();
		} else {
			minZ = loc2.getBlockZ();
			maxZ = loc1.getBlockZ();
		}
		min = new Location(world, minX, minY, minZ);
		max = new Location(world, maxX, maxY, maxZ);
	}

	@Override
	public Location getMinimumPoint() {
		return min;
	}

	@Override
	public Location getMaximumPoint() {
		return max;
	}

	@Override
	public World getWorld() {
		return world;
	}

	@Override
	public int getArea() {
		return (max.getBlockX() - min.getBlockX()) * (max.getBlockZ() - min.getBlockZ())
				* (max.getBlockY() - min.getBlockY());
	}

	@Override
	public boolean contains(Location pt) {
		int x = pt.getBlockX();
		int y = pt.getBlockY();
		int z = pt.getBlockZ();
		boolean inX = (x < max.getBlockX() && x > min.getBlockX());
		boolean inY = (y < max.getBlockY() && y > min.getBlockY());
		boolean inZ = (z < max.getBlockZ() && z > min.getBlockZ());
		return inX && inY && inZ;
	}

}
