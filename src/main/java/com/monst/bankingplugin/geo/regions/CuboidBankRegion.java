package com.monst.bankingplugin.geo.regions;

import com.monst.bankingplugin.geo.Vector2D;
import com.monst.bankingplugin.geo.Vector3D;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.*;

/**
 * This class represents a region of space in the shape of a rectangular prism. It is defined by a {@link World} and
 * a minimum and a maximum {@link Vector3D} point in space.
 */
public class CuboidBankRegion extends BankRegion {

	private final Block min;
	private final Block max;

	/**
	 * Creates a new {@link CuboidBankRegion} and the specified attributes
	 *
	 * @param world the world the region is in
	 * @param loc1 the first corner bound (any combination of upper/lower x, y, z values)
	 * @param loc2 the other corner bound
	 * @return a new CuboidBankRegion
	 */
	public static CuboidBankRegion of(World world, Vector3D loc1, Vector3D loc2) {
		Block min = world.getBlockAt(
				Math.min(loc1.getX(), loc2.getX()),
				Math.min(loc1.getY(), loc2.getY()),
				Math.min(loc1.getZ(), loc2.getZ())
		);
		Block max = world.getBlockAt(
				Math.max(loc1.getX(), loc2.getX()),
				Math.max(loc1.getY(), loc2.getY()),
				Math.max(loc1.getZ(), loc2.getZ())
		);
		return new CuboidBankRegion(world, min, max);
	}

	private CuboidBankRegion(World world, Block min, Block max) {
		super(world);
		this.min = min;
		this.max = max;
	}

	@Override
	public Block getMinimumBlock() {
		return min;
	}

	@Override
	public Block getMaximumBlock() {
		return max;
	}

	@Override
	public Block getCenterPoint() {
		int centerX = (getMaxX() + getMinX()) / 2;
		int centerY = (getMaxY() + getMinY()) / 2;
		int centerZ = (getMaxZ() + getMinZ()) / 2;
		return world.getBlockAt(centerX, centerY, centerZ);
	}

	@Override
	public int getMinX() { return min.getX(); }
	@Override
	public int getMaxX() { return max.getX(); }
	@Override
	public int getMinY() { return min.getY(); }
	@Override
	public int getMaxY() { return max.getY(); }
	@Override
	public int getMinZ() { return min.getZ(); }
	@Override
	public int getMaxZ() { return max.getZ(); }

	@Override
	public String getCoordinates() {
		return "(" + getMinX() + ", " + getMinY() + ", " + getMinZ()
				+ ") -> ("
				+ getMaxX() + ", " + getMaxY() + ", " + getMaxZ() + ")";
	}

	@Override
	public long getVolume() {
		return (long) getLength() * getWidth() * getHeight();
	}

	@Override
	public boolean overlaps(BankRegion region) {
		if (isDisjunct(region))
			return false;
		Set<Vector2D> blocks = getFootprint();
		return region.getFootprint().stream().anyMatch(blocks::contains);
	}

	@Override
	public Set<Vector2D> getFootprint() {
		Set<Vector2D> blocks = new HashSet<>();
		int maxX = getMaxX();
		int maxZ = getMaxZ();
		for (int x = getMinX(); x <= maxX; x++)
			for (int z = getMinZ(); z <= maxZ; z++)
				blocks.add(new Vector2D(x, z));
		return blocks;
	}

	@Override
	public List<Location> getCorners() {
		List<Location> vertices = new ArrayList<>();
		for (int x : new int[] { getMinX(), getMaxX() })
			for (int y : new int[] { getMinY(), getMaxY() })
				for (int z : new int[] { getMinZ(), getMaxZ() })
					vertices.add(new Location(world, x, y, z));
		return vertices;
	}

	@Override
	public boolean contains(int x, int z) {
		return overlapsX(x, x) && overlapsZ(z, z);
	}

	@Override
	public boolean isCuboid() {
		return true;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		CuboidBankRegion otherRegion = (CuboidBankRegion) o;
		return     Objects.equals(min, otherRegion.min)
				&& Objects.equals(max, otherRegion.max)
				&& Objects.equals(world, otherRegion.world);
	}

	@Override
	public int hashCode() {
		return Objects.hash(getMinX(), getMaxX(), getMinY(), getMaxY(), getMinZ(), getMaxZ(), world);
	}

}
