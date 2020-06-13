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
		int minX = points.stream().mapToInt(BlockVector2D::getBlockX).min().getAsInt();
		int minZ = points.stream().mapToInt(BlockVector2D::getBlockZ).min().getAsInt();
		return new Location(world, minX, minY, minZ);
	}

	@Override
	public Location getMaximumPoint() {
		int maxX = points.stream().mapToInt(BlockVector2D::getBlockX).max().getAsInt();
		int maxZ = points.stream().mapToInt(BlockVector2D::getBlockZ).max().getAsInt();
		return new Location(world, maxX, maxY, maxZ);
	}

	@Override
	public World getWorld() {
		return world;
	}

	@Override
	public int getArea() {
		int minX = getMinimumPoint().getBlockX();
		int maxX = getMaximumPoint().getBlockX();
		int minZ = getMinimumPoint().getBlockZ();
		int maxZ = getMaximumPoint().getBlockZ();
		return (maxX - minX) * (maxY - minY) * (maxZ - minZ);
	}

	@Override
	public boolean contains(Location pt) {
		int x = pt.getBlockX();
		int y = pt.getBlockY();
		int z = pt.getBlockZ();
		Location max = getMaximumPoint();
		Location min = getMinimumPoint();
		return (x < max.getBlockX() && x > min.getBlockX()) && (y < max.getBlockY() 
				&& y > min.getBlockY()) && (z < max.getBlockZ() && z > max.getBlockZ());
	}

}