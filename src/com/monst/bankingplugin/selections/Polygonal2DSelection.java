package com.monst.bankingplugin.selections;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.World;

import com.monst.bankingplugin.utils.BlockVector2D;

public class Polygonal2DSelection implements Selection {

	private World world;
	private List<BlockVector2D> points;
	private int minY;
	private int maxY;

	public Polygonal2DSelection(World world, List<BlockVector2D> points, int minY, int maxY) {
		this.world = world;
		this.points = points;
		this.minY = Math.min(Math.max(0, minY), world.getMaxHeight());
		this.maxY = Math.min(Math.max(0, maxY), world.getMaxHeight());
	}

	public List<BlockVector2D> getNativePoints() {
		return points;
	}

	@Override
	public Location getMinimumPoint() {
		return null;
	}

	@Override
	public Location getMaximumPoint() {
		return null;
	}

	@Override
	public World getWorld() {
		return null;
	}

	@Override
	public int getArea() {
		return 0;
	}

	@Override
	public boolean contains(Location pt) {
		return false;
	}

}