package com.monst.bankingplugin.selections;

import org.bukkit.Location;
import org.bukkit.World;

import java.awt.*;
import java.util.Collection;
import java.util.HashSet;

public class CuboidSelection implements Selection {

	private final World world;
	private final Location min;
	private final Location max;
	private final Rectangle rect;

	public static CuboidSelection of(World world, Location loc1, Location loc2) {
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
		Location min = new Location(world, minX, minY, minZ);
		Location max = new Location(world, maxX, maxY, maxZ);
		Rectangle rectangle = new Rectangle(minX, minZ, (maxX - minX), (maxZ - minZ));
		return new CuboidSelection(world, min, max, rectangle);
	}

	private CuboidSelection(World world, Location min, Location max, Rectangle rectangle) {
		this.world = world;
		this.min = min;
		this.max = max;
		this.rect = rectangle;
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
	public Location getCenterPoint() {
		int centerY = (max.getBlockY() + min.getBlockY()) / 2;
		return new Location(getWorld(), (int) rect.getCenterX(), centerY, (int) rect.getCenterY());
	}

	@Override
	public World getWorld() {
		return world;
	}

	@Override
	public String getCoordinates() {
		Location min = getMinimumPoint();
		Location max = getMaximumPoint();
		return "(" + min.getBlockX() + ", " + min.getBlockY() + ", " + min.getBlockZ() + ") -> (" + max.getBlockX()
				+ ", " + max.getBlockY() + ", " + max.getBlockZ() + ")";
	}

	@Override
	public int getVolume() {
		   return (max.getBlockX() - min.getBlockX())
				* (max.getBlockZ() - min.getBlockZ())
				* (max.getBlockY() - min.getBlockY());
	}

	@Override
	public Shape getShape() {
		return rect;
	}

	@Override
	public Collection<Location> getVertices() {
		Collection<Location> vertices = new HashSet<>();
		vertices.add(min);
		vertices.add(new Location(world, max.getX(), min.getY(), min.getZ()));
		vertices.add(new Location(world, min.getX(), max.getY(), min.getZ()));
		vertices.add(new Location(world, max.getX(), max.getY(), min.getZ()));
		vertices.add(new Location(world, min.getX(), min.getY(), max.getZ()));
		vertices.add(new Location(world, max.getX(), min.getY(), max.getZ()));
		vertices.add(new Location(world, min.getX(), max.getY(), max.getZ()));
		vertices.add(max);
		return vertices;
	}

	@Override
	public boolean contains(Location pt) {
		int x = pt.getBlockX();
		int y = pt.getBlockY();
		int z = pt.getBlockZ();
		boolean inX = (x <= max.getBlockX() && x >= min.getBlockX());
		boolean inY = (y <= max.getBlockY() && y >= min.getBlockY());
		boolean inZ = (z <= max.getBlockZ() && z >= min.getBlockZ());
		return inX && inY && inZ;
	}

	@Override
	public SelectionType getType() {
		return SelectionType.CUBOID;
	}

}
