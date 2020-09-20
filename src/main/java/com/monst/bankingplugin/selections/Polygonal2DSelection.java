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
	private final List<BlockVector2D> vertices;
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

	private Polygonal2DSelection(World world, List<BlockVector2D> vertices, int minY, int maxY) {
		this.world = world;
		this.vertices = vertices;
		this.minY = minY;
		this.maxY = maxY;
	}

	public List<BlockVector2D> getNativePoints() {
		return vertices;
	}

	@Override
	@SuppressWarnings("all")
	public BlockVector3D getMinimumPoint() {
		int minX = vertices.stream().mapToInt(BlockVector2D::getBlockX).min().getAsInt();
		int minZ = vertices.stream().mapToInt(BlockVector2D::getBlockZ).min().getAsInt();
		return new BlockVector3D(minX, minY, minZ);
	}

	@Override
	@SuppressWarnings("all")
	public BlockVector3D getMaximumPoint() {
		int maxX = vertices.stream().mapToInt(BlockVector2D::getBlockX).max().getAsInt();
		int maxZ = vertices.stream().mapToInt(BlockVector2D::getBlockZ).max().getAsInt();
		return new BlockVector3D(maxX, maxY, maxZ);
	}

	@Override
	public BlockVector3D getCenterPoint() {
		BlockVector3D max = getMaximumPoint();
		BlockVector3D min = getMinimumPoint();
		int centerX = (max.getBlockX() + min.getBlockX()) / 2;
		int centerY = (maxY + minY) / 2;
		int centerZ = (max.getBlockZ() + min.getBlockZ()) / 2;
		return new BlockVector3D(centerX, centerY, centerZ);
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
	public Collection<BlockVector3D> getVertices() {
		Collection<BlockVector3D> vertices = new HashSet<>();
		this.vertices.forEach(point -> {
			vertices.add(new BlockVector3D(point.getBlockX(), minY, point.getBlockZ()));
			vertices.add(new BlockVector3D(point.getBlockX(), maxY, point.getBlockZ()));
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
		BlockVector3D min = getMinimumPoint();
		BlockVector3D max = getMaximumPoint();
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
		if (vertices.size() < 3)
			return false;
		if (pt.getBlockY() < minY || pt.getBlockY() > maxY)
			return false;
		if (pt.getWorld() != null && !pt.getWorld().equals(getWorld()))
			return false;
		return contains(new BlockVector2D(pt.getBlockX(), pt.getBlockZ()));
	}

	@Override
	public boolean contains(BlockVector2D vector) {
		int pointX = vector.getBlockX(); //width
		int pointZ = vector.getBlockZ(); //depth

		int nextX, nextZ, x1, z1, x2, z2;
		int prevX = vertices.get(vertices.size() - 1).getBlockX();
		int prevZ = vertices.get(vertices.size() - 1).getBlockZ();

		long crossProduct;
		boolean inside = false;
		for (BlockVector2D point : vertices) {
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