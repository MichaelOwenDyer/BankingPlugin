package com.monst.bankingplugin.selections;

import com.monst.bankingplugin.utils.Utils;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class CuboidSelection implements Selection {

	private final World world;
	private final Location min;
	private final Location max;

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
		return new CuboidSelection(world, min, max);
	}

	private CuboidSelection(World world, Location min, Location max) {
		this.world = world;
		this.min = min;
		this.max = max;
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
		int centerX = (max.getBlockX() + min.getBlockX()) / 2;
		int centerY = (max.getBlockY() + min.getBlockY()) / 2;
		int centerZ = (max.getBlockZ() + min.getBlockZ()) / 2;
		return new Location(getWorld(), centerX, centerY, centerZ);
	}

	@Override
	public int getMinY() {
		return min.getBlockY();
	}

	@Override
	public int getMaxY() {
		return max.getBlockY();
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
	public long getVolume() {
		return (max.getBlockX() - min.getBlockX() + 1)
			 * (max.getBlockZ() - min.getBlockZ() + 1)
			 * (max.getBlockY() - min.getBlockY() + 1);
	}

	@Override
	public boolean overlaps(Selection sel) {
		if (getMinY() > sel.getMaxY() || getMaxY() < sel.getMinY())
			return false;
		Set<BlockVector2D> blocks = getBlocks();
		return Utils.filter(sel.getBlocks(), blocks::contains, Collectors.toSet()).isEmpty();
	}

	@Override
	public Set<BlockVector2D> getBlocks() {
		Set<BlockVector2D> blocks = new HashSet<>();
		for (int x = min.getBlockX(); x <= max.getBlockX(); x++)
			for (int z = min.getBlockZ(); z <= max.getBlockZ(); z++)
				blocks.add(new BlockVector2D(x, z));
		return blocks;
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
		if (pt.getWorld() != null && !pt.getWorld().equals(getWorld()))
			return false;
		int y = pt.getBlockY();
		return (y <= max.getBlockY() && y >= min.getBlockY()) &&
				contains(new BlockVector2D(pt.getBlockX(), pt.getBlockZ()));
	}

	@Override
	public boolean contains(BlockVector2D bv) {
		int x = bv.getBlockX();
		int z = bv.getBlockZ();
		return (x <= max.getBlockX() && x >= min.getBlockX()) && (z <= max.getBlockZ() && z >= min.getBlockZ());
	}

	@Override
	public SelectionType getType() {
		return SelectionType.CUBOID;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		CuboidSelection otherSel = ((CuboidSelection) o);
		return getWorld().equals(otherSel.getWorld())
				&& getMaximumPoint().equals(otherSel.getMaximumPoint())
				&& getMinimumPoint().equals(otherSel.getMinimumPoint());
	}

}
