package com.monst.bankingplugin.selections;

import com.monst.bankingplugin.utils.BlockVector2D;
import org.bukkit.Location;
import org.bukkit.World;

import java.awt.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class Polygonal2DSelection implements Selection {

	private final World world;
	private final List<BlockVector2D> points;
	private final int minY;
	private final int maxY;
	private final Polygon poly;

	public Polygonal2DSelection(World world, List<BlockVector2D> points, int minY, int maxY) {
		this.world = world;
		this.points = points;
		this.minY = Math.min(Math.max(0, minY), world.getMaxHeight());
		this.maxY = Math.min(Math.max(0, maxY), world.getMaxHeight());

		int[] xpoints = new int[points.size()];
		int[] ypoints = new int[points.size()];
		for (int i = 0; i < points.size(); i++) {
			xpoints[i] = points.get(i).getBlockX();
			ypoints[i] = points.get(i).getBlockZ();
		}
		poly = new Polygon(xpoints, ypoints, points.size());
	}

	public List<BlockVector2D> getNativePoints() {
		return points;
	}

	@Override
	@SuppressWarnings("all")
	public Location getMinimumPoint() {
		int minX = points.stream().mapToInt(BlockVector2D::getBlockX).min().getAsInt();
		int minZ = points.stream().mapToInt(BlockVector2D::getBlockZ).min().getAsInt();
		return new Location(world, minX, minY, minZ);
	}

	@Override
	@SuppressWarnings("all")
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
	public String getCoordinates() {
		int minY = getMinimumPoint().getBlockY();
		int maxY = getMaximumPoint().getBlockY();
		return getNativePoints().stream().map(vec -> "(" + vec.getBlockX() + ", " + vec.getBlockZ() + ")")
				.collect(Collectors.joining(", ")) + " at Y = " + minY + " to " + maxY;
	}

	@Override
	public int getVolume() {
		int area = 0;
		for (int i = 0; i < points.size() - 1; i++)
			area += (points.get(i + 1).getBlockX() * points.get(i).getBlockZ())
					- (points.get(i + 1).getBlockZ() * points.get(i).getBlockX());
		return area;
	}

	@Override
	public Collection<Location> getVertices() {
		Collection<Location> vertices = new HashSet<>();
		points.forEach(point -> {
			vertices.add(new Location(world, point.getBlockX(), minY, point.getBlockZ()));
			vertices.add(new Location(world, point.getBlockX(), maxY, point.getBlockZ()));
		});
		return vertices;
	}

	@Override
	public Shape getShape() {
		return poly;
	}

	@Override
	public boolean contains(Location pt) {
		return pt.getBlockY() >= minY && pt.getBlockY() <= maxY
				&& poly.contains(new Point(pt.getBlockX(), pt.getBlockZ()));
	}

	@Override
	public SelectionType getType() {
		return SelectionType.POLYGONAL;
	}

}