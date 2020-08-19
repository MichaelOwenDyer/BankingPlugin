package com.monst.bankingplugin.selections;

import com.monst.bankingplugin.utils.BlockVector2D;
import org.bukkit.Location;
import org.bukkit.World;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class Polygonal2DSelection implements Selection {

	private final World world;
	private final List<BlockVector2D> points;
	private final int minY;
	private final int maxY;
	private final Polygon polygon;

	public static Polygonal2DSelection of(World world, List<BlockVector2D> points, int minY, int maxY) {
		int[] xpoints = new int[points.size()];
		int[] ypoints = new int[points.size()];
		for (int i = 0; i < points.size(); i++) {
			xpoints[i] = points.get(i).getBlockX();
			ypoints[i] = points.get(i).getBlockZ();
		}
		if (minY > maxY) {
			int temp = minY;
			minY = maxY;
			maxY = temp;
		}
		return new Polygonal2DSelection(
				world,
				points,
				Math.min(Math.max(0, minY), world.getMaxHeight()),
				Math.min(Math.max(0, maxY), world.getMaxHeight()),
				new Polygon(xpoints, ypoints, points.size())
		);
	}

	private Polygonal2DSelection(World world, List<BlockVector2D> points, int minY, int maxY, Polygon polygon) {
		this.world = world;
		this.points = points;
		this.minY = minY;
		this.maxY = maxY;
		this.polygon = polygon;
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

	public int getMinY() {
		return minY;
	}

	public int getMaxY() {
		return maxY;
	}

	@Override
	public Location getCenterPoint() {
		Location max = getMaximumPoint();
		Location min = getMinimumPoint();
		int centerX, centerY, centerZ;
		centerX = (max.getBlockX() + min.getBlockX()) / 2;
		centerY = (maxY + minY) / 2;
		centerZ = (max.getBlockZ() + min.getBlockZ()) / 2;
		return new Location(getWorld(), centerX, centerY, centerZ);
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
				.collect(Collectors.joining(", ")) + " at " + minY + " ≤ y ≤ " + maxY;
	}

	@Override
	public long getVolume() {
		long area = 0;
		Rectangle bounds = polygon.getBounds();
		for (int x = (int) bounds.getMinX(); x <= Math.ceil(bounds.getMaxX()); x++)
			for (int z = (int) bounds.getMinY(); z <= Math.ceil(bounds.getMaxY()); z++)
				if (contains(new Location(world, x, minY, z)))
					area++;
		return (maxY - minY + 1) * area;
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
		return polygon;
	}

	@Override
	public boolean contains(Location pt) {
	 	if (pt.getWorld() != null && !pt.getWorld().equals(world))
			return false;
		if (points.size() < 3)
			return false;
		if (pt.getBlockY() < minY || pt.getBlockY() > maxY)
			return false;

		int pointX = pt.getBlockX(); //width
		int pointZ = pt.getBlockZ(); //depth

		int nextX, nextZ, x1, z1, x2, z2;
		int prevX = points.get(points.size() - 1).getBlockX();
		int prevZ = points.get(points.size() - 1).getBlockZ();

		long crossProduct;
		boolean inside = false;
		for (BlockVector2D point : points) {
			nextX = point.getBlockX();
			nextZ = point.getBlockZ();
			if (nextX == pointX && nextZ == pointZ) // Location is on a vertex
				return true;
			if (nextX > prevX) {
				x1 = prevX;
				x2 = nextX;
				z1 = prevZ;
				z2 = nextZ;
			} else {
				x1 = nextX;
				x2 = prevX;
				z1 = nextZ;
				z2 = prevZ;
			}
			if (x1 <= pointX && pointX <= x2) {
				crossProduct = ((pointZ - z1) * (x2 - x1)) - ((z2 - z1) * (pointX - x1));
				if (crossProduct == 0) {
					if ((z1 <= pointZ) == (pointZ <= z2))
						return true; //Location is on edge between vertices
				} else if (crossProduct < 0 && (x1 != pointX))
					inside = !inside;
			}
			prevX = nextX;
			prevZ = nextZ;
		}
		return inside;
	}

	@Override
	public SelectionType getType() {
		return SelectionType.POLYGONAL;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		Polygonal2DSelection other = ((Polygonal2DSelection) o);
		return getMinY() == other.getMinY() && getMaxY() == other.getMaxY()
				&& getNativePoints().equals(other.getNativePoints())
				&& getWorld().equals(other.getWorld());
	}

}