package com.monst.bankingplugin.selections;

import org.bukkit.Location;
import org.bukkit.World;

public class CuboidSelection implements Selection {

	private World world;
	private Location min;
	private Location max;

	public CuboidSelection(World world, Location loc1, Location loc2) {
		this.world = world;
		if (loc1.getBlockY() == loc2.getBlockY()) {
			if (loc1.getBlockX() == loc2.getBlockX()) {
				if (loc1.getBlockZ() == loc2.getBlockZ()) {
					min = loc1;
					max = loc2;
				} else {
					min = loc1.getBlockZ() < loc2.getBlockZ() ? loc1 : loc2;
					max = loc1.getBlockZ() > loc2.getBlockZ() ? loc1 : loc2;
				}
			} else {
				min = loc1.getBlockX() < loc2.getBlockX() ? loc1 : loc2;
				max = loc1.getBlockX() > loc2.getBlockX() ? loc1 : loc2;
			}
		} else {
			min = loc1.getBlockY() < loc2.getBlockY() ? loc1 : loc2;
			max = loc1.getBlockY() > loc2.getBlockY() ? loc1 : loc2;
		}
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
		return (x < max.getBlockX() && x > min.getBlockX()) && (y < max.getBlockY() && y > min.getBlockY())
				&& (z < max.getBlockZ() && z > min.getBlockZ());
	}

}
