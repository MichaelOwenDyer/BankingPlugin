package com.monst.bankingplugin.selections;

import org.bukkit.Location;
import org.bukkit.World;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Polygonal2DSelection implements Selection {

	private final World world;
	private final List<BlockVector2D> points;
	private final int minY;
	private final int maxY;

	public static Polygonal2DSelection of(World world, List<BlockVector2D> points, int minY, int maxY) {
		if (minY > maxY) {
			int temp = minY;
			minY = maxY;
			maxY = temp;
		}
		return new Polygonal2DSelection(
				world,
				points,
				Math.min(Math.max(0, minY), world.getMaxHeight()),
				Math.min(Math.max(0, maxY), world.getMaxHeight())
		);
	}

	private Polygonal2DSelection(World world, List<BlockVector2D> points, int minY, int maxY) {
		this.world = world;
		this.points = points;
		this.minY = minY;
		this.maxY = maxY;
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
	public int getMinY() {
		return minY;
	}

	@Override
	public int getMaxY() {
		return maxY;
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
		return (maxY - minY + 1) * getBlocks().size();
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
	public boolean overlaps(Selection sel) {
		if (getMinY() > sel.getMaxY() || getMaxY() < sel.getMinY())
			return false;
		Set<BlockVector2D> blocks = sel.getBlocks();
		return getBlocks().stream().anyMatch(blocks::contains);
	}

	@Override
	public Set<BlockVector2D> getBlocks() {
		Set<BlockVector2D> blocks = new HashSet<>();
		Location min = getMinimumPoint();
		Location max = getMaximumPoint();
		for (int x = min.getBlockX(); x <= max.getBlockX(); x++)
			for (int z = min.getBlockZ(); z <= max.getBlockZ(); z++) {
				BlockVector2D bv = new BlockVector2D(x, z);
				if (contains(bv))
					blocks.add(bv);
			}
		return blocks;
	}

	@Override
	public boolean contains(Location pt) {
		if (pt.getWorld() != null && !pt.getWorld().equals(getWorld()))
			return false;
		if (points.size() < 3)
			return false;
		if (pt.getBlockY() < minY || pt.getBlockY() > maxY)
			return false;
		return contains(new BlockVector2D(pt.getBlockX(), pt.getBlockZ()));
	}

	@Override
	public boolean contains(BlockVector2D vector) {
		int pointX = vector.getBlockX(); //width
		int pointZ = vector.getBlockZ(); //depth

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